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

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.GraphQLSchema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.common.gateway.graphql.FieldComplexityCalculatorImpl;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * This Handler can be used to analyse GraphQL Query. This implementation uses previously set
 * complexity and depth limitation to block the complex queries before it reaches the backend.
 */
public class GraphQLQueryAnalysisHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(GraphQLQueryAnalysisHandler.class);
    private GraphQLSchema schema = null;

    public boolean handleRequest(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        if ((axis2MC.getIncomingTransportName().equals("ws") || axis2MC.getIncomingTransportName().equals("wss")) &&
                (boolean) messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)){
            return true;
        }
        schema = (GraphQLSchema) messageContext.getProperty(APIConstants.GRAPHQL_SCHEMA);
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
     * This method returns the maximum query complexity value
     *
     * @param messageContext message context of the request
     * @return maximum query depth value if exists, or -1 to denote no complexity limitation
     */
    private int getMaxQueryDepth(MessageContext messageContext) {
        Object maxQueryDepth = messageContext.getProperty(APIConstants.MAXIMUM_QUERY_DEPTH);
        if (maxQueryDepth != null) {
            int maxDepth = ((Integer) maxQueryDepth).intValue();
            if (maxDepth > 0) {
                return maxDepth;
            } else {
                log.debug("Maximum query depth value is 0");
                return -1;
            }
        } else {
            log.debug("Maximum query depth not applicable");
            return -1;
        }
    }

    /**
     * This method analyses the query
     *
     * @param messageContext message context of the request
     * @param payload        payload of the request
     * @return true, if the query is not blocked or false, if the query is blocked
     */
    private boolean analyseQuery(MessageContext messageContext, String payload) {

        try {
            if (analyseQueryDepth(messageContext, payload) &&
                    analyseQueryComplexity(messageContext, payload)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            String errorMessage = "Policy definition parsing failed. ";
            log.error(errorMessage, e);
            handleFailure(messageContext);
            return false;
        }
    }
    /**
     * This method handle the failure
     *
     * @param messageContext   message context of the request
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

    /**
     * This method analyses the query depth
     *
     * @param messageContext message context of the request
     * @param payload        payload of the request
     * @return true, if the query depth does not exceed the maximum value or false, if query depth exceeds the maximum
     */
    private boolean analyseQueryDepth(MessageContext messageContext, String payload) {
        int maxQueryDepth = getMaxQueryDepth(messageContext);

        if (maxQueryDepth > 0) {
            MaxQueryDepthInstrumentation maxQueryDepthInstrumentation =
                    new MaxQueryDepthInstrumentation(maxQueryDepth);
            GraphQL runtime = GraphQL.newGraphQL(schema).instrumentation(maxQueryDepthInstrumentation).build();

            try {
                ExecutionResult executionResult = runtime.execute(payload);
                List<GraphQLError> errors = executionResult.getErrors();
                if (errors.size() > 0) {
                    List<String> errorList = new ArrayList<>();
                    for (GraphQLError error : errors) {
                        errorList.add(error.getMessage());
                    }

                    // TODO: https://github.com/wso2/carbon-apimgt/issues/8147
                    ListIterator<String> iterator = errorList.listIterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().contains("non-nullable")) {
                            iterator.remove();
                        }
                    }
                    if (errorList.size() == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Maximum query depth of " + maxQueryDepth + " was not exceeded");
                        }
                        return true;
                    }
                    handleFailure(GraphQLConstants.GRAPHQL_QUERY_TOO_DEEP, messageContext,
                            GraphQLConstants.GRAPHQL_QUERY_TOO_DEEP_MESSAGE, errorList.toString());
                    log.error(errorList.toString());
                    return false;
                }
                return true;
            } catch (Throwable e) {
                log.error(e);
            }
        } else {
            return true; // No depth limitation check
        }
        return false;
    }

    /**
     * This method analyses the query complexity
     *
     * @param messageContext message context of the request
     * @param payload        payload of the request
     * @return true, if query complexity does not exceed the maximum or false, if query complexity exceeds the maximum
     */
    private boolean analyseQueryComplexity(MessageContext messageContext, String payload) {
        FieldComplexityCalculator fieldComplexityCalculator = new GraphQLComplexityCalculator(messageContext);
        int maxQueryComplexity = getMaxQueryComplexity(messageContext);

        if (maxQueryComplexity > 0) {
            MaxQueryComplexityInstrumentation maxQueryComplexityInstrumentation =
                    new MaxQueryComplexityInstrumentation(maxQueryComplexity, fieldComplexityCalculator);
            GraphQL runtime = GraphQL.newGraphQL(schema).instrumentation(maxQueryComplexityInstrumentation).build();

            try {
                ExecutionResult executionResult = runtime.execute(payload);
                List<GraphQLError> errors = executionResult.getErrors();
                if (errors.size() > 0) {
                    List<String> errorList = new ArrayList<>();
                    for (GraphQLError error : errors) {
                        errorList.add(error.getMessage());
                    }

                    // TODO: https://github.com/wso2/carbon-apimgt/issues/8147
                    ListIterator<String> iterator = errorList.listIterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().contains("non-nullable")) {
                            iterator.remove();
                        }
                    }
                    if (errorList.size() == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Maximum query complexity was not exceeded");
                        }
                        return true;
                    } else {
                        log.error(errorList);
                        errorList.clear();
                        errorList.add("maximum query complexity exceeded");
                    }
                    handleFailure(GraphQLConstants.GRAPHQL_QUERY_TOO_COMPLEX, messageContext,
                            GraphQLConstants.GRAPHQL_QUERY_TOO_COMPLEX_MESSAGE, errorList.toString());
                    return false;
                }
                return true;
            } catch (Throwable e) {
                log.error(e);
            }
        } else {
            return true; // No complexity limitation check
        }
        return false;
    }

    /**
     * This method returns the maximum query complexity value
     *
     * @param messageContext message context of the request
     * @return maximum query complexity value if exists, or -1 to denote no complexity limitation
     */
    private int getMaxQueryComplexity(MessageContext messageContext) {
        Object maxQueryComplexity = messageContext.getProperty(APIConstants.MAXIMUM_QUERY_COMPLEXITY);
        if (maxQueryComplexity != null) {
            int maxComplexity = ((Integer) maxQueryComplexity).intValue();
            if (maxComplexity > 0) {
                return maxComplexity;
            } else {
                log.debug("Maximum query complexity value is 0");
                return -1;
            }
        } else {
            log.debug("Maximum query complexity not applicable");
            return -1;
        }
    }

    /**
     * This method handle the failure
     *
     * @param errorCodeValue   error code of the failure
     * @param messageContext   message context of the request
     * @param errorMessage     error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleFailure(int errorCodeValue, MessageContext messageContext, String errorMessage, String errorDescription) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCodeValue);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        Mediator sequence = messageContext.getSequence(GraphQLConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_BAD_REQUEST);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }
}

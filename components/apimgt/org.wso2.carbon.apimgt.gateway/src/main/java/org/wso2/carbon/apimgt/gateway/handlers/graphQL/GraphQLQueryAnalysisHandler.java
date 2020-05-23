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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.usermgt.APIKeyMgtRemoteUserClient;
import org.wso2.carbon.apimgt.gateway.handlers.security.usermgt.APIKeyMgtRemoteUserClientPool;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class GraphQLQueryAnalysisHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(GraphQLQueryAnalysisHandler.class);
    private GraphQLSchema schema = null;

    public boolean handleRequest(MessageContext messageContext) {
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
     * @param policyDefinition json object which contains the policy
     * @return maximum query depth value if exists, or -1 to denote no complexity limitation
     */
    private int  getMaxQueryDepth(JSONObject policyDefinition) {
        Object depthObject = policyDefinition.get(APIConstants.QUERY_ANALYSIS_DEPTH);
        Object maxQueryDepth = ((JSONObject) depthObject).get(APIConstants.MAXIMUM_QUERY_DEPTH);
        if (maxQueryDepth != null) {
            return ((Long) maxQueryDepth).intValue();
        } else {
            log.error("Maximum query depth was not allocated");
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
        JSONParser jsonParser = new JSONParser();

        try {
            String graphQLAccessControlPolicy =
                    (String) messageContext.getProperty(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY);
            JSONObject policyDefinition = (JSONObject) jsonParser.parse(graphQLAccessControlPolicy);
            if (analyseQueryDepth(messageContext, payload, policyDefinition) &&
                    analyseQueryComplexity(messageContext, payload, policyDefinition)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            log.error("Policy definition parsing failed. " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * This method analyses the query depth
     *
     * @param messageContext   message context of the request
     * @param payload          payload of the request
     * @param policyDefinition json object which contains the policy
     * @return true, if the query depth does not exceed the maximum value or false, if query depth exceeds the maximum
     */
    private boolean analyseQueryDepth(MessageContext messageContext, String payload, JSONObject policyDefinition) {
        int maxQueryDepth = getMaxQueryDepth(policyDefinition);

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

                        handleFailure(APISecurityConstants.GRAPHQL_QUERY_TOO_DEEP, messageContext,
                                APISecurityConstants.GRAPHQL_QUERY_TOO_DEEP_MESSAGE, errorList.toString());
                        log.error(errorList.toString());
                        return false;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Maximum query depth of " + maxQueryDepth + " was not exceeded");
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
     * @param messageContext   message context of the request
     * @param payload          payload of the request
     * @param policyDefinition json object which contains the policy
     * @return true, if query complexity does not exceed the maximum or false, if query complexity exceeds the maximum
     */
    private boolean analyseQueryComplexity(MessageContext messageContext, String payload, JSONObject policyDefinition) {
        FieldComplexityCalculator fieldComplexityCalculator = new FieldComplexityCalculatorImpl(messageContext);
        int maxQueryComplexity = getMaxQueryComplexity(policyDefinition);

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

                    handleFailure(APISecurityConstants.GRAPHQL_QUERY_TOO_COMPLEX, messageContext,
                            APISecurityConstants.GRAPHQL_QUERY_TOO_COMPLEX_MESSAGE, errorList.toString());
                    return false;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Maximum query complexity was not exceeded");
                }
                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            return true; // No complexity limitation check
        }
        return false;
    }

    /**
     * This method returns the maximum query complexity value
     *
     * @param policyDefinition json object which contains the policy
     * @return maximum query complexity value if exists, or -1 to denote no complexity limitation
     */
    private int getMaxQueryComplexity(JSONObject policyDefinition) {
        Object complexityObject = policyDefinition.get(APIConstants.QUERY_ANALYSIS_COMPLEXITY);
        Object maxQueryComplexity = ((JSONObject) complexityObject).get(APIConstants.MAXIMUM_QUERY_COMPLEXITY);
        if (maxQueryComplexity != null) {
            return ((Long) maxQueryComplexity).intValue();
        } else {
            log.error("Maximum query complexity was not allocated");
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
    private void handleFailure(int errorCodeValue, MessageContext messageContext,
                               String errorMessage, String errorDescription) {
        OMElement payload = getFaultPayload(errorCodeValue, errorMessage, errorDescription);
        Utils.setFaultPayload(messageContext, payload);
        Mediator sequence = messageContext.getSequence(APISecurityConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * @param errorCodeValue error code
     * @param message        fault message
     * @param description    description of the fault message
     * @return the OMElement
     */
    private OMElement getFaultPayload(int errorCodeValue, String message, String description) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(errorCodeValue + "");
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(message);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(description);

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

}

/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.inbound.websocket.request;

import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
import graphql.validation.Validator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.QueryAnalyzerResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.graphql.QueryValidator;
import org.wso2.carbon.apimgt.gateway.dto.GraphQLOperationDTO;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.GraphQLConstants;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.analyzer.SubscriptionAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.utils.GraphQLProcessorUtil;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.GraphQLProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import java.util.List;

/**
 * A GraphQL subscriptions specific extension of RequestProcessor. This class intercepts the inbound websocket
 * execution path of graphQL subscription requests.
 */
public class GraphQLRequestProcessor extends RequestProcessor {

    private static final Log log = LogFactory.getLog(GraphQLRequestProcessor.class);

    /**
     * Handle inbound websocket requests for GraphQL subscriptions and perform authentication, authorization,
     * payload validation, query depth and complexity analysis and throttling.
     *
     * @param msgSize               Message size of graphQL subscription payload
     * @param msgText               The GraphQL subscription payload text
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO
     */
    @Override
    public InboundProcessorResponseDTO handleRequest(int msgSize, String msgText,
                                                     InboundMessageContext inboundMessageContext) {

        InboundProcessorResponseDTO responseDTO;
        JSONObject graphQLMsg = new JSONObject(msgText);
        responseDTO = InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext);
        Parser parser = new Parser();

        //for gql subscription operation payloads
        if (!responseDTO.isError() && checkIfSubscribeMessage(graphQLMsg)) {
            String operationId = graphQLMsg.getString(
                    GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID);
            if (validatePayloadFields(graphQLMsg)) {
                String graphQLSubscriptionPayload =
                        ((JSONObject) graphQLMsg.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD))
                                .getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_QUERY);
                Document document = parser.parseDocument(graphQLSubscriptionPayload);
                // Extract the operation type and operations from the payload
                OperationDefinition operation = getOperationFromPayload(document);
                if (operation != null) {
                    if (checkIfValidSubscribeOperation(operation)) {
                        responseDTO = validateQueryPayload(inboundMessageContext, document, operationId);
                        if (!responseDTO.isError()) {
                            // subscription operation name
                            String subscriptionOperation = GraphQLProcessorUtil.getOperationList(operation,
                                    inboundMessageContext.getGraphQLSchemaDTO().getTypeDefinitionRegistry());
                            WebSocketUtils.setApiPropertyToChannel(inboundMessageContext.getCtx(),
                                    APIConstants.API_ELECTED_RESOURCE, subscriptionOperation);
                            // extract verb info dto with throttle policy for matching verb
                            VerbInfoDTO verbInfoDTO = InboundWebsocketProcessorUtil
                                    .findMatchingVerb(subscriptionOperation, inboundMessageContext);
                            String authType = verbInfoDTO.getAuthType();
                            // validate scopes based on subscription payload when security is enabled
                            if (!StringUtils.capitalize(APIConstants.AUTH_TYPE_NONE.toLowerCase()).equals(authType)) {
                                responseDTO = InboundWebsocketProcessorUtil
                                        .validateScopes(inboundMessageContext, subscriptionOperation, operationId);
                            }
                            if (!responseDTO.isError()) {
                                SubscriptionAnalyzer subscriptionAnalyzer = new SubscriptionAnalyzer(
                                        inboundMessageContext.getGraphQLSchemaDTO().getGraphQLSchema());
                                // analyze query depth and complexity
                                responseDTO = validateQueryDepthAndComplexity(subscriptionAnalyzer,
                                        inboundMessageContext, graphQLSubscriptionPayload, operationId);
                                if (!responseDTO.isError()) {
                                    //throttle for matching resource
                                    responseDTO = InboundWebsocketProcessorUtil
                                            .doThrottleForGraphQL(msgSize, verbInfoDTO, inboundMessageContext,
                                                    operationId);
                                    // add verb info dto for the successful invoking subscription operation request
                                    inboundMessageContext.addVerbInfoForGraphQLMsgId(graphQLMsg
                                                    .getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID),
                                            new GraphQLOperationDTO(verbInfoDTO, subscriptionOperation));
                                }
                            }
                        }
                    } else {
                        responseDTO = InboundWebsocketProcessorUtil.getBadRequestGraphQLFrameErrorDTO(
                                "Invalid operation. Only allowed Subscription type operations", operationId);
                    }
                } else {
                    responseDTO = InboundWebsocketProcessorUtil.getBadRequestGraphQLFrameErrorDTO(
                            "Operation definition cannot be empty", operationId);
                }
            } else {
                responseDTO = InboundWebsocketProcessorUtil.getBadRequestGraphQLFrameErrorDTO(
                        "Invalid operation payload", operationId);
            }
        }
        return responseDTO;
    }

    /**
     * Check if message has mandatory graphql subscription payload and id fields. Payload should consist 'type' field
     * and its value equal to either of 'start' or 'subscribe'. The value 'start' is used in
     * 'subscriptions-transport-ws' protocol and 'subscribe' is used in 'graphql-ws' protocol.
     *
     * @param graphQLMsg GraphQL message JSON object
     * @return true if valid subscribe message
     */
    private boolean checkIfSubscribeMessage(JSONObject graphQLMsg) {
        return graphQLMsg.getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE) != null
                && GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ARRAY_FOR_SUBSCRIBE.contains(
                graphQLMsg.getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE))
                && graphQLMsg.getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID) != null;
    }

    /**
     * Validate message fields 'payload' and 'query'.
     * Example valid payload: 'payload':{query: subscription { greetings }}'
     *
     * @param graphQLMsg GraphQL message JSON object
     * @return true if valid payload fields present
     */
    private boolean validatePayloadFields(JSONObject graphQLMsg) {
        return graphQLMsg.has(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)
                && graphQLMsg.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD) != null
                && ((JSONObject) graphQLMsg.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD))
                .has(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_QUERY)
                && ((JSONObject) graphQLMsg.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD))
                .get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_QUERY) != null;
    }

    /**
     * Get GraphQL Operation from payload.
     *
     * @param document GraphQL payload
     * @return Operation definition
     */
    private OperationDefinition getOperationFromPayload(Document document) {

        OperationDefinition operation = null;
        // Extract the operation type and operations from the payload
        for (Definition definition : document.getDefinitions()) {
            if (definition instanceof OperationDefinition) {
                operation = (OperationDefinition) definition;
                break;
            }
        }
        return operation;
    }

    /**
     * Check if graphql operation is a Subscription operation.
     *
     * @param operation GraphQL operation
     * @return true if valid operation type
     */
    private boolean checkIfValidSubscribeOperation(OperationDefinition operation) {
        return operation.getOperation() != null
                && APIConstants.GRAPHQL_SUBSCRIPTION.equalsIgnoreCase(operation.getOperation().toString());
    }

    /**
     * Validates GraphQL query payload using QueryValidator and graphql schema of the invoking API.
     *
     * @param inboundMessageContext InboundMessageContext
     * @param document              Graphql payload
     * @param operationId           Graphql message id
     * @return InboundProcessorResponseDTO
     */
    private InboundProcessorResponseDTO validateQueryPayload(InboundMessageContext inboundMessageContext,
                                                             Document document, String operationId) {

        GraphQLProcessorResponseDTO responseDTO = new GraphQLProcessorResponseDTO();
        responseDTO.setId(operationId);
        QueryValidator queryValidator = new QueryValidator(new Validator());
        // payload validation
        String validationErrorMessage = queryValidator.validatePayload(
                inboundMessageContext.getGraphQLSchemaDTO().getGraphQLSchema(), document);
        if (validationErrorMessage != null) {
            String error = WebSocketApiConstants.FrameErrorConstants.GRAPHQL_INVALID_QUERY_MESSAGE + " : "
                    + validationErrorMessage;
            log.error(error);
            responseDTO.setError(true);
            responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_INVALID_QUERY);
            responseDTO.setErrorMessage(error);
            return responseDTO;
        }
        return responseDTO;
    }

    /**
     * Validate query depth and complexity of graphql subscription payload.
     *
     * @param subscriptionAnalyzer  Query complexity and depth analyzer for subscription operations
     * @param inboundMessageContext InboundMessageContext
     * @param payload               GraphQL payload
     * @param operationId           Graphql message id
     * @return GraphQLProcessorResponseDTO
     */
    private GraphQLProcessorResponseDTO validateQueryDepthAndComplexity(SubscriptionAnalyzer subscriptionAnalyzer,
                                                                        InboundMessageContext inboundMessageContext,
                                                                        String payload, String operationId) {

        GraphQLProcessorResponseDTO responseDTO = validateQueryDepth(subscriptionAnalyzer, inboundMessageContext,
                payload, operationId);
        if (!responseDTO.isError()) {
            return validateQueryComplexity(subscriptionAnalyzer, inboundMessageContext, payload, operationId);
        }
        return responseDTO;
    }

    /**
     * Validate query complexity of graphql subscription payload.
     *
     * @param subscriptionAnalyzer  Query complexity and depth analyzer for subscription operations
     * @param inboundMessageContext InboundMessageContext
     * @param payload               GraphQL payload
     * @param operationId           Graphql message id
     * @return GraphQLProcessorResponseDTO
     */
    private GraphQLProcessorResponseDTO validateQueryComplexity(SubscriptionAnalyzer subscriptionAnalyzer,
                                                                InboundMessageContext inboundMessageContext,
                                                                String payload, String operationId) {

        GraphQLProcessorResponseDTO responseDTO = new GraphQLProcessorResponseDTO();
        responseDTO.setId(operationId);
        try {
            QueryAnalyzerResponseDTO queryAnalyzerResponseDTO =
                    subscriptionAnalyzer.analyseSubscriptionQueryComplexity(payload,
                            inboundMessageContext.getInfoDTO().getGraphQLMaxComplexity());
            if (!queryAnalyzerResponseDTO.isSuccess() && !queryAnalyzerResponseDTO.getErrorList().isEmpty()) {
                List<String> errorList = queryAnalyzerResponseDTO.getErrorList();
                log.error("Query complexity validation failed for: " + payload + " errors: " + errorList.toString());
                responseDTO.setError(true);
                responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_COMPLEX);
                responseDTO.setErrorMessage(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_COMPLEX_MESSAGE
                        + " : " + queryAnalyzerResponseDTO.getErrorList().toString());
                return responseDTO;
            }
        } catch (APIManagementException e) {
            log.error("Error while validating query complexity for: " + payload, e);
            responseDTO.setError(true);
            responseDTO.setErrorMessage(e.getMessage());
            responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.INTERNAL_SERVER_ERROR);
        }
        return responseDTO;
    }

    /**
     * Validate query depth of graphql subscription payload.
     *
     * @param subscriptionAnalyzer  Query complexity and depth analyzer for subscription operations
     * @param inboundMessageContext InboundMessageContext
     * @param payload               GraphQL payload
     * @param operationId           GraphQL message Id
     * @return GraphQLProcessorResponseDTO
     */
    private GraphQLProcessorResponseDTO validateQueryDepth(SubscriptionAnalyzer subscriptionAnalyzer,
                                                           InboundMessageContext inboundMessageContext,
                                                           String payload, String operationId) {

        GraphQLProcessorResponseDTO responseDTO = new GraphQLProcessorResponseDTO();
        responseDTO.setId(operationId);
        QueryAnalyzerResponseDTO queryAnalyzerResponseDTO =
                subscriptionAnalyzer.analyseSubscriptionQueryDepth(inboundMessageContext.getInfoDTO().
                        getGraphQLMaxDepth(), payload);
        if (!queryAnalyzerResponseDTO.isSuccess() && !queryAnalyzerResponseDTO.getErrorList().isEmpty()) {
            List<String> errorList = queryAnalyzerResponseDTO.getErrorList();
            log.error("Query depth validation failed for: " + payload + " errors: " + errorList.toString());
            responseDTO.setError(true);
            responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP);
            responseDTO.setErrorMessage(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP_MESSAGE
                    + " : " + queryAnalyzerResponseDTO.getErrorList().toString());
            return responseDTO;
        }
        return responseDTO;
    }
}

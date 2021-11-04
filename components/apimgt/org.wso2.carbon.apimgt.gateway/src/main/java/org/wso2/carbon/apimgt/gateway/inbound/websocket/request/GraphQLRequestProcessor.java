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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.QueryAnalyzerResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.graphql.QueryValidator;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.GraphQLConstants;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.analyzer.SubscriptionAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.utils.GraphQLProcessorUtil;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.GraphQLOperationDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import java.util.List;

public class GraphQLRequestProcessor extends RequestProcessor {

    private static final Log log = LogFactory.getLog(GraphQLRequestProcessor.class);

    @Override
    public InboundProcessorResponseDTO handleRequest(int msgSize, String msgText,
                                                     InboundMessageContext inboundMessageContext) {

        InboundProcessorResponseDTO responseDTO;
        JSONObject graphQLMsg = new JSONObject(msgText);
        responseDTO = InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext);
        Parser parser = new Parser();

        //for gql subscription operation payloads
        if (checkIfSubscribeMessage(graphQLMsg)) {
            if (validatePayloadFields(graphQLMsg)) {
                String graphQLSubscriptionPayload =
                        ((JSONObject) graphQLMsg.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD))
                                .getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_QUERY);
                Document document = parser.parseDocument(graphQLSubscriptionPayload);
                // Extract the operation type and operations from the payload
                OperationDefinition operation = getOperationFromPayload(document);
                if (operation != null) {
                    if (checkIfValidSubscribeOperation(operation, graphQLMsg)) {
                        if (!validateQueryPayload(inboundMessageContext, document).isError()) {
                            String subscriptionOperation = GraphQLProcessorUtil.getOperationList(operation,
                                    inboundMessageContext.getGraphQLSchemaDTO().getTypeDefinitionRegistry());
                            // validate scopes based on subscription payload
                            if (!InboundWebsocketProcessorUtil
                                    .validateScopes(inboundMessageContext, subscriptionOperation).isError()) {
                                // extract verb info dto with throttle policy for matching verb
                                VerbInfoDTO verbInfoDTO = InboundWebsocketProcessorUtil.findMatchingVerb(
                                        subscriptionOperation, inboundMessageContext);
                                inboundMessageContext
                                        .addVerbInfoForGraphQLMsgId(graphQLMsg.getString(
                                                        GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID),
                                                new GraphQLOperationDTO(verbInfoDTO, subscriptionOperation));
                                SubscriptionAnalyzer subscriptionAnalyzer =
                                        new SubscriptionAnalyzer(inboundMessageContext.getGraphQLSchemaDTO()
                                                .getGraphQLSchema());
                                // analyze query depth and complexity
                                if (!validateQueryDepthAndComplexity(subscriptionAnalyzer, inboundMessageContext,
                                        graphQLSubscriptionPayload).isError()) {
                                    //throttle for matching resource
                                    return InboundWebsocketProcessorUtil.doThrottle(msgSize, verbInfoDTO,
                                            inboundMessageContext);
                                }
                            }
                        }
                    } else {
                        responseDTO = InboundWebsocketProcessorUtil.getBadRequestFrameErrorDTO(
                                "Invalid operation. Only allowed Subscription type operations");
                    }
                } else {
                    responseDTO = InboundWebsocketProcessorUtil.getBadRequestFrameErrorDTO(
                            "Operation definition cannot be empty");
                }
            } else {
                responseDTO = InboundWebsocketProcessorUtil.getBadRequestFrameErrorDTO(
                        "Invalid operation payload");
            }
        }
        return responseDTO;
    }

    private boolean checkIfSubscribeMessage(JSONObject graphQLMsg) {
        return graphQLMsg.getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE) != null
                && GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ARRAY_FOR_SUBSCRIBE.contains(
                graphQLMsg.getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE));
    }

    private boolean validatePayloadFields(JSONObject graphQLMsg) {
        return graphQLMsg.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD) != null
                && ((JSONObject) graphQLMsg.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD))
                .get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_QUERY) != null;
    }

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

    private boolean checkIfValidSubscribeOperation(OperationDefinition operation, JSONObject graphQLMsg) {
        return operation.getOperation() != null
                && APIConstants.GRAPHQL_SUBSCRIPTION.equalsIgnoreCase(operation.getOperation().toString())
                && graphQLMsg.getString(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID) != null;
    }

    private InboundProcessorResponseDTO validateQueryPayload(InboundMessageContext inboundMessageContext,
                                                             Document document) {

        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        QueryValidator queryValidator = new QueryValidator(new Validator());
        // payload validation
        String validationErrorMessage = queryValidator.validatePayload(
                inboundMessageContext.getGraphQLSchemaDTO().getGraphQLSchema(), document);
        if (validationErrorMessage != null) {
            String error = GraphQLConstants.GRAPHQL_INVALID_QUERY_MESSAGE + " : " + validationErrorMessage;
            log.error(error);
            responseDTO.setError(true);
            responseDTO.setErrorMessage(error);
            return responseDTO;
        }
        return responseDTO;
    }

    private InboundProcessorResponseDTO validateQueryDepthAndComplexity(SubscriptionAnalyzer subscriptionAnalyzer,
                                                                        InboundMessageContext inboundMessageContext,
                                                                        String payload) {
        InboundProcessorResponseDTO responseDTO = validateQueryDepth(subscriptionAnalyzer, inboundMessageContext,
                payload);
        if (!responseDTO.isError()) {
            return validateQueryComplexity(subscriptionAnalyzer, inboundMessageContext, payload);
        }
        return responseDTO;
    }

    private InboundProcessorResponseDTO validateQueryComplexity(SubscriptionAnalyzer subscriptionAnalyzer,
                                                                InboundMessageContext inboundMessageContext,
                                                                String payload) {

        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        try {
            QueryAnalyzerResponseDTO queryAnalyzerResponseDTO =
                    subscriptionAnalyzer.analyseQueryComplexity(payload,
                            inboundMessageContext.getInfoDTO().getGraphQLMaxComplexity());
            if (!queryAnalyzerResponseDTO.isSuccess() && !queryAnalyzerResponseDTO.getErrorList().isEmpty()) {
                List<String> errorList = queryAnalyzerResponseDTO.getErrorList();
                log.error("Query complexity validation failed for: " + payload + " errors: " + errorList.toString());
                responseDTO.setError(true);
                responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_COMPLEX);
                responseDTO.setErrorMessage(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_COMPLEX +
                        " : " + queryAnalyzerResponseDTO.getErrorList().toString());
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

    private InboundProcessorResponseDTO validateQueryDepth(SubscriptionAnalyzer subscriptionAnalyzer,
                                                           InboundMessageContext inboundMessageContext,
                                                           String payload) {

        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        try {
            QueryAnalyzerResponseDTO queryAnalyzerResponseDTO =
                    subscriptionAnalyzer.analyseQueryComplexity(payload,
                            inboundMessageContext.getInfoDTO().getGraphQLMaxComplexity());
            if (!queryAnalyzerResponseDTO.isSuccess() && !queryAnalyzerResponseDTO.getErrorList().isEmpty()) {
                List<String> errorList = queryAnalyzerResponseDTO.getErrorList();
                log.error("Query depth validation failed for: " + payload + " errors: " + errorList.toString());
                responseDTO.setError(true);
                responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_COMPLEX);
                responseDTO.setErrorMessage(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP_MESSAGE
                        + " : " + queryAnalyzerResponseDTO.getErrorList().toString());
                return responseDTO;
            }
        } catch (APIManagementException e) {
            log.error("Error while validating query depth for: " + payload, e);
            responseDTO.setError(true);
            responseDTO.setErrorMessage(e.getMessage());
            responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.INTERNAL_SERVER_ERROR);
        }
        return responseDTO;
    }
}

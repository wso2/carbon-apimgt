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

import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.validation.Validator;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.common.gateway.graphql.QueryValidator;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.utils.GraphQLProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Set;

import static org.apache.axis2.Constants.Configuration.HTTP_METHOD;

public class GraphQLAPIHandler extends AbstractHandler {

    private static final String QUERY_PATH_STRING = "/?query=";
    private static final String QUERY_PAYLOAD_STRING = "query";
    private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    private static final String GRAPHQL_API = "GRAPHQL";
    private static final String HTTP_VERB = "HTTP_VERB";
    private static final String UNICODE_TRANSFORMATION_FORMAT = "UTF-8";
    private static final Log log = LogFactory.getLog(GraphQLAPIHandler.class);
    private GraphQLSchemaDTO graphQLSchemaDTO;
    private String apiUUID;
    private QueryValidator queryValidator;

    public GraphQLAPIHandler() {

        queryValidator = new QueryValidator(new Validator());
    }

    public String getApiUUID() {

        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {

        this.apiUUID = apiUUID;
    }

    public boolean handleRequest(MessageContext messageContext) {
        try {
            if (Utils.isGraphQLSubscriptionRequest(messageContext)) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping GraphQL subscription handshake request.");
                }
                return true;
            }
            String payload;
            Parser parser = new Parser();
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();
            String requestPath = messageContext.getProperty(REST_SUB_REQUEST_PATH).toString();
            if (requestPath != null && !requestPath.isEmpty()) {
                String[] queryParams = ((Axis2MessageContext) messageContext).getProperties().
                        get(REST_SUB_REQUEST_PATH).toString().split(QUERY_PATH_STRING);
                if (queryParams.length > 1) {
                    payload = URLDecoder.decode(queryParams[1], UNICODE_TRANSFORMATION_FORMAT);
                } else {
                    RelayUtils.buildMessage(axis2MC);
                    OMElement body = axis2MC.getEnvelope().getBody().getFirstElement();
                    if (body != null && body.getFirstChildWithName(QName.valueOf(QUERY_PAYLOAD_STRING)) != null){
                        payload = body.getFirstChildWithName(QName.valueOf(QUERY_PAYLOAD_STRING)).getText();
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Invalid query parameter " + queryParams[0]);
                        }
                        handleFailure(messageContext, "Invalid query parameter");
                        return false;
                    }
                }
                messageContext.setProperty(APIConstants.GRAPHQL_PAYLOAD, payload);
            } else {
                handleFailure(messageContext, "Request path cannot be empty");
                return false;
            }

            // Validate payload with graphQLSchema
            Document document = parser.parseDocument(payload);

            if (validatePayloadWithSchema(messageContext, document)) {
                supportForBasicAndAuthentication(messageContext);

                // Extract the operation type and operations from the payload
                for (Definition definition : document.getDefinitions()) {
                    if (definition instanceof OperationDefinition) {
                        OperationDefinition operation = (OperationDefinition) definition;
                        if (operation.getOperation() != null) {
                            String httpVerb = ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                                    getProperty(HTTP_METHOD).toString();
                            messageContext.setProperty(HTTP_VERB, httpVerb);
                            ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty(HTTP_METHOD,
                                    operation.getOperation().toString());
                            String operationList = GraphQLProcessorUtil.getOperationList(operation,
                                    graphQLSchemaDTO.getTypeDefinitionRegistry());
                            messageContext.setProperty(APIConstants.API_ELECTED_RESOURCE, operationList);
                            if (log.isDebugEnabled()) {
                                log.debug("Operation list has been successfully added to elected property");
                            }
                            return true;
                        }
                    } else {
                        handleFailure(messageContext, "Operation definition cannot be empty");
                        return false;
                    }
                }
            } else {
                return false;
            }
        } catch (IOException | XMLStreamException | InvalidSyntaxException e) {
            log.error(e.getMessage());
            handleFailure(messageContext, e.getMessage());
        }
        return false;
    }

    /**
     * Support GraphQL APIs for basic,JWT  authentication, this method extract the scopes and operations from
     * local Entry and set them to properties. If the operations have scopes, scopes operation mapping and scope
     * role mappings are added to schema as additional types before adding them to local entry
     *
     * @param messageContext message context of the request
     */
    private void supportForBasicAndAuthentication(MessageContext messageContext) {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        HashMap<String, String> operationThrottlingMappingList = new HashMap<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        HashMap<String, Boolean> operationAuthSchemeMappingList = new HashMap<>();
        HashMap<String, String> operationScopeMappingList = new HashMap<>();
        HashMap<String, ArrayList<String>> scopeRoleMappingList = new HashMap<>();
        String graphQLAccessControlPolicy = null;

        if (graphQLSchemaDTO.getGraphQLSchema() != null) {
            Set<GraphQLType> additionalTypes = graphQLSchemaDTO.getGraphQLSchema().getAdditionalTypes();
            for (Object additionalType : additionalTypes.toArray()) {
                if (additionalType instanceof GraphQLObjectType) {
                    String additionalTypeName = ((GraphQLObjectType) additionalType).getName();
                    if (additionalTypeName.startsWith(APIConstants.GRAPHQL_ADDITIONAL_TYPE_PREFIX)) {
                        ArrayList<String> roleArrayList = new ArrayList<>();
                        String[] additionalTypeNameArray = additionalTypeName.split("_", 2);
                        String typeValue;
                        if (additionalTypeNameArray.length > 1) {
                            typeValue = additionalTypeNameArray[1];
                        } else {
                            typeValue = additionalTypeNameArray[0];
                        }

                        String base64DecodedTypeValue = new String(Base64.getUrlDecoder().decode(typeValue));
                        for (GraphQLFieldDefinition fieldDefinition : ((GraphQLObjectType) additionalType)
                                .getFieldDefinitions()) {
                            if (additionalTypeName.contains(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY)) {
                                graphQLAccessControlPolicy = new String(
                                        Base64.getUrlDecoder().decode(fieldDefinition.getName()));
                            }
                            // Fill in each list according to the relevant field definition
                            setMappingList(additionalTypeName, base64DecodedTypeValue, fieldDefinition,
                                    operationThrottlingMappingList, operationAuthSchemeMappingList,
                                    operationScopeMappingList, roleArrayList);
                        }
                        if (!roleArrayList.isEmpty()) {
                            scopeRoleMappingList.put(base64DecodedTypeValue, roleArrayList);
                            if (log.isDebugEnabled()) {
                                log.debug("Added scope " + base64DecodedTypeValue + "with role list " + String
                                        .join(",", roleArrayList));
                            }
                        }
                    }
                }
            }
        }

        messageContext.setProperty(APIConstants.SCOPE_ROLE_MAPPING, scopeRoleMappingList);
        messageContext.setProperty(APIConstants.SCOPE_OPERATION_MAPPING, operationScopeMappingList);
        messageContext.setProperty(APIConstants.OPERATION_THROTTLING_MAPPING, operationThrottlingMappingList);
        messageContext.setProperty(APIConstants.OPERATION_AUTH_SCHEME_MAPPING, operationAuthSchemeMappingList);
        messageContext.setProperty(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY, graphQLAccessControlPolicy);
        messageContext.setProperty(APIConstants.API_TYPE, GRAPHQL_API);
        messageContext.setProperty(APIConstants.GRAPHQL_SCHEMA, graphQLSchemaDTO.getGraphQLSchema());
    }

    private void setMappingList(String additionalTypeName, String base64DecodedTypeValue,
            GraphQLFieldDefinition fieldDefinition, HashMap<String, String> operationThrottlingMappingList,
            HashMap<String, Boolean> operationAuthSchemeMappingList, HashMap<String, String> operationScopeMappingList,
            ArrayList<String> roleArrayList) {

        String base64DecodedURLTypeName = new String(Base64.getUrlDecoder().decode(fieldDefinition.getName()));
        if (additionalTypeName.contains(APIConstants.SCOPE_ROLE_MAPPING)) {
            roleArrayList.add(base64DecodedURLTypeName);
            if (log.isDebugEnabled()) {
                log.debug("Added scope " + base64DecodedTypeValue + "with role " + base64DecodedURLTypeName);
            }
        } else if (additionalTypeName.contains(APIConstants.SCOPE_OPERATION_MAPPING)) {
            operationScopeMappingList.put(base64DecodedTypeValue, base64DecodedURLTypeName);
            if (log.isDebugEnabled()) {
                log.debug("Added operation " + base64DecodedTypeValue + "with scope " + base64DecodedURLTypeName);
            }
        } else if (additionalTypeName.contains(APIConstants.OPERATION_THROTTLING_MAPPING)) {
            operationThrottlingMappingList.put(base64DecodedTypeValue, base64DecodedURLTypeName);
            if (log.isDebugEnabled()) {
                log.debug("Added operation " + base64DecodedTypeValue + "with throttling " + base64DecodedURLTypeName);
            }
        } else if (additionalTypeName.contains(APIConstants.OPERATION_AUTH_SCHEME_MAPPING)) {
            boolean isSecurityEnabled = true;
            if (APIConstants.OPERATION_SECURITY_DISABLED.equalsIgnoreCase(fieldDefinition.getName())) {
                isSecurityEnabled = false;
            }
            operationAuthSchemeMappingList.put(base64DecodedTypeValue, isSecurityEnabled);
            if (log.isDebugEnabled()) {
                log.debug("Added operation " + base64DecodedTypeValue + "with security " + isSecurityEnabled);
            }
        }
    }

    /**
     * This method validate the payload
     *
     * @param messageContext message context of the request
     * @param document       graphQL schema of the request
     * @return true or false
     */
    private boolean validatePayloadWithSchema(MessageContext messageContext, Document document) {

        String validationErrorMessage;
        // Get GraphQL schema data from gateway internal data holder
        graphQLSchemaDTO = DataHolder.getInstance().getApiToGraphQLSchemaDTOMap().get(apiUUID);
        validationErrorMessage = queryValidator.validatePayload(graphQLSchemaDTO.getGraphQLSchema(), document);
        if (validationErrorMessage != null) {
            handleFailure(messageContext, validationErrorMessage);
            return false;
        }
        return true;
    }

    /**
     * This method handle the failure
     *
     * @param messageContext   message context of the request
     * @param errorDescription description of the error
     */
    private void handleFailure(MessageContext messageContext, String errorDescription) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, GraphQLConstants.GRAPHQL_INVALID_QUERY);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, GraphQLConstants.GRAPHQL_INVALID_QUERY_MESSAGE);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        Mediator sequence = messageContext.getSequence(GraphQLConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }
}



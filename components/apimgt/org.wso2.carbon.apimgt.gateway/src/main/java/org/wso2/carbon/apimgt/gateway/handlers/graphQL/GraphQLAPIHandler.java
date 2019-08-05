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

import graphql.schema.GraphQLType;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.language.Selection;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.util.Base64;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

public class GraphQLAPIHandler extends AbstractHandler {

    private static final String QUERY_PATH_STRING = "/?query=";
    private static final String GRAPHQL_API_OPERATION_RESOURCE = "OPERATION_RESOURCE";
    private static final String GRAPHQL_SCOPE_ROLE_MAPPING = "GRAPHQL_SCOPE_ROLE_MAPPING";
    private static final String GRAPHQL_SCOPE_OPERATION_MAPPING = "GRAPHQL_SCOPE_OPERATION_MAPPING";
    private static final String GRAPHQL_API_OPERATION_TYPE = "OPERATION_TYPE";
    private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    private static final String API_TYPE = "API_TYPE";
    private static final String GRAPHQL_API = "GRAPHQL";
    private static final String UNICODE_TRANSFORMATION_FORMAT = "UTF-8";
    private static final String INVALID_QUERY = "INVALID QUERY";

    private static final String scopeRoleMapping = "ScopeRoleMapping";
    private static final String scopeOperationMapping = "ScopeOperationMapping";
    private static final String graphQLIdentifier = "_graphQL";
    private static final String classNameAndMethod = "_GraphQLAPIHandler_handleRequest";
    private static final Log log = LogFactory.getLog(GraphQLAPIHandler.class);
    private static GraphQLSchema schema = null;
    private static Validator validator;

    public GraphQLAPIHandler() {
        validator = new Validator();
    }
    private String apiUUID;

    public String getApiUUID() {

        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {

        this.apiUUID = apiUUID;
    }

    public boolean handleRequest(MessageContext messageContext) {
        try {
            String validationErrorMessage;
            String operationList = "";
            String payload;
            Parser parser = new Parser();

            ArrayList<String> roleArrayList = new ArrayList<>();
            ArrayList<String> operationArray = new ArrayList<>();
            ArrayList<String> validationErrorMessageList = new ArrayList<>();
            HashMap<String, ArrayList<String>> scopeRoleMappingList = new HashMap<>();
            HashMap<String, String> operationScopeMappingList = new HashMap<>();
            List<ValidationError> validationErrors;

            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();
            String requestPath = ((Axis2MessageContext) messageContext).getProperties().
                    get(REST_SUB_REQUEST_PATH).toString();
            if (!requestPath.isEmpty()) {
                String[] queryParams = ((Axis2MessageContext) messageContext).getProperties().
                        get(REST_SUB_REQUEST_PATH).toString().split(QUERY_PATH_STRING);
                if (queryParams.length > 1) {
                    String queryURLValue = queryParams[1];
                    payload = URLDecoder.decode(queryURLValue, UNICODE_TRANSFORMATION_FORMAT);
                } else {
                    RelayUtils.buildMessage(axis2MC);
                    OMElement body = axis2MC.getEnvelope().getBody().getFirstElement();
                    if (body != null && body.getFirstElement() != null) {
                        payload = body.getFirstElement().getText();
                    } else {
                        log.debug("Invalid query parameter " + queryParams[0]);
                        handleFailure(messageContext, "Invalid query parameter");
                        return false;
                    }
                }
            } else {
                handleFailure(messageContext, "Request path cannot be empty");
                return false;
            }

            // Validate payload with graphQLSchema
            Document document = parser.parseDocument(payload);
            synchronized (apiUUID + classNameAndMethod) {
                if (schema == null) {
                    Entry localEntryObj = (Entry) messageContext.getConfiguration().getLocalRegistry().get(apiUUID +
                                graphQLIdentifier);
                    if (localEntryObj != null) {
                        SchemaParser schemaParser = new SchemaParser();
                        TypeDefinitionRegistry registry = schemaParser.parse(localEntryObj.getValue().toString());
                        schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
                    }
                }
            }

            validationErrors = validator.validateDocument(schema, document);
            if (validationErrors != null && validationErrors.size() > 0) {
                for (ValidationError error : validationErrors) {
                    validationErrorMessageList.add(error.getDescription());
                }
                validationErrorMessage = String.join(",", validationErrorMessageList);
                handleFailure(messageContext, validationErrorMessage);
                return false;
            }

            // To support GraphQL APIs for basic,JWT  authentication
            // Extract the scopes and operations from local Entry and set them to Properties
            // If the operations have scopes, scopes operation mapping and scope role mappings are added to
            // schema as additional types before adding them to local entry
            if (schema != null) {
                Set<GraphQLType> additionalTypes = schema.getAdditionalTypes();
                for (GraphQLType additionalType : additionalTypes) {
                    String base64DecodedAdditionalType = new String(Base64.getUrlDecoder().decode(additionalType.getName().
                            split("_", 2)[1]));
                    for (GraphQLType type : additionalType.getChildren()) {
                        if (additionalType.getName().contains(scopeRoleMapping)) {
                            String base64DecodedURLRole = new String(Base64.getUrlDecoder().decode(type.getName()));
                            roleArrayList = new ArrayList<>();
                            roleArrayList.add(base64DecodedURLRole);
                        } else if (additionalType.getName().contains(scopeOperationMapping)) {
                            String base64DecodedURLScope = new String(Base64.getUrlDecoder().decode(type.getName()));
                            operationScopeMappingList.put(base64DecodedAdditionalType, base64DecodedURLScope);
                        }
                    }
                    if (!roleArrayList.isEmpty()) {
                        scopeRoleMappingList.put(base64DecodedAdditionalType, roleArrayList);
                    }
                }
            }

            messageContext.setProperty(GRAPHQL_SCOPE_ROLE_MAPPING, scopeRoleMappingList);
            messageContext.setProperty(GRAPHQL_SCOPE_OPERATION_MAPPING, operationScopeMappingList);
            messageContext.setProperty(API_TYPE, GRAPHQL_API);

            // Extract the operation type and operations from the payload
            for (Definition definition : document.getDefinitions()) {
                if (definition instanceof OperationDefinition) {
                    OperationDefinition operation = (OperationDefinition) definition;
                    if (operation.getOperation() != null) {
                        ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                                setProperty(Constants.Configuration.HTTP_METHOD , operation.getOperation().toString());
//                        messageContext.setProperty(Constants.Configuration.HTTP_METHOD, operation.getOperation());
                        if (Operation.QUERY.equals(operation.getOperation())) {
                            for (Selection selection : operation.getSelectionSet().getSelections()) {
                                if (selection instanceof Field) {
                                    Field field = (Field) selection;
                                    operationArray.add(field.getName());
                                    log.debug("Operation - Query " + field.getName());
                                }
                            }
                            //sort them to unique the operationlistCachekey for all the combination of operations
                            Collections.sort(operationArray);
                            operationList = String.join(",", operationArray);
                        } else if (operation.getOperation().equals(Operation.MUTATION)) {
                            operationList = operation.getName();
                            log.debug("Operation - Mutation " + operation.getName());
                        } else if (operation.getOperation().equals(Operation.SUBSCRIPTION)) {
                            operationList = operation.getName();
                            log.debug("Operation - Subscription " + operation.getName());
                        }
                        messageContext.setProperty(APIConstants.API_ELECTED_RESOURCE, operationList);
                        return true;
                    }
                } else {
                    handleFailure(messageContext, "Operation definition cannot be empty");
                    return false;
                }
            }
        } catch (IOException | XMLStreamException e) {
            log.error(e.getMessage());
            handleFailure(messageContext, e.getMessage());
        }
        return false;
    }

    private void handleFailure(MessageContext messageContext, String errorMessage) {
        OMElement payload = getFaultPayload(errorMessage);
        setFaultPayload(messageContext, payload);
        sendFault(messageContext);
    }

    private static void setFaultPayload(MessageContext messageContext, OMElement payload) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        JsonUtil.removeJsonPayload(axis2MC);
        messageContext.getEnvelope().getBody().addChild(payload);
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String acceptType = (String) headers.get(HttpHeaders.ACCEPT);
        Set<String> supportedMimes = new HashSet<>(Arrays.asList("application/x-www-form-urlencoded",
                "multipart/form-data",
                "text/html",
                "application/xml",
                "text/xml",
                "application/soap+xml",
                "text/plain",
                "application/json",
                "application/json/badgerfish",
                "text/javascript"));

        // If an Accept header has been provided and is supported by the Gateway
        if (!StringUtils.isEmpty(acceptType) && supportedMimes.contains(acceptType)) {
            axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, acceptType);
        } else {
            // If there isn't Accept Header in the request, will use error_message_type property
            // from _auth_failure_handler_.xml file
            if (messageContext.getProperty("error_message_type") != null) {
                axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE,
                        messageContext.getProperty("error_message_type"));
            }
        }
    }

    private static void sendFault(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        axis2MC.setProperty(NhttpConstants.HTTP_SC, HttpStatus.SC_UNPROCESSABLE_ENTITY);
        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);
        axis2MC.removeProperty("NO_ENTITY_BODY");

        // Always remove the ContentType - Let the formatter do its thing
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            headers.remove(HttpHeaders.AUTHORIZATION);
            headers.remove(HttpHeaders.AUTHORIZATION);
            headers.remove(HttpHeaders.HOST);
        }
        Axis2Sender.sendBack(messageContext);
    }

    private OMElement getFaultPayload(String message) {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(HttpStatus.SC_UNPROCESSABLE_ENTITY + "");
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(INVALID_QUERY);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(message);

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


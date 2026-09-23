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
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.validation.Validator;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.common.gateway.graphql.QueryValidator;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.common.gateway.graphql.GraphQLProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.apache.axis2.Constants.Configuration.HTTP_METHOD;

public class GraphQLAPIHandler extends AbstractHandler {

    private static final String QUERY_PATH_STRING = "/?query=";
    private static final String QUERY_PAYLOAD_STRING = "query";
    private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    private static final String GRAPHQL_API = "GRAPHQL";
    private static final String HTTP_VERB = "HTTP_VERB";
    private static final String UNICODE_TRANSFORMATION_FORMAT = "UTF-8";
    // Per the GraphQL spec, introspection meta-fields (__schema, __type, __typename) always start with "__".
    // They are never declared in an API's SDL, so they can never appear in the schema-derived elected-resource
    // list - they must be detected explicitly instead of silently falling through as "no match".
    private static final String INTROSPECTION_FIELD_PREFIX = "__";
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
                            // Check for introspection fields before electing a resource: a top-level selection
                            // set may contain an introspection field ALONGSIDE a genuine schema-defined field
                            // (e.g. "{ characters { id } __schema { types { name } } }") - in that case
                            // getOperationListAsString below would still resolve non-empty (from the matched
                            // field), so introspection must be checked unconditionally rather than only when
                            // the elected-resource list turns out empty.
                            List<String> introspectionFields = getIntrospectionFieldNames(operation);
                            if (!introspectionFields.isEmpty()) {
                                handleIntrospectionNotSupported(messageContext, introspectionFields);
                                return false;
                            }
                            String operationList = GraphQLProcessorUtil.getOperationListAsString(operation,
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
     * Returns the top-level GraphQL introspection meta-field names (e.g. "__schema", "__type") present in the
     * given operation's selection set, if any. Introspection fields are never declared in an API's SDL, so they
     * are never part of the schema-derived "supported fields" used for resource election - without this explicit
     * check they would silently be dropped, leaving an empty elected resource that is indistinguishable from a
     * request for a genuinely nonexistent resource.
     *
     * @param operation the parsed GraphQL operation
     * @return the introspection field names found in the top-level selection set, or an empty list if none
     */
    private List<String> getIntrospectionFieldNames(OperationDefinition operation) {

        List<String> introspectionFields = new ArrayList<>();
        for (Selection selection : operation.getSelectionSet().getSelections()) {
            if (selection instanceof Field) {
                String fieldName = ((Field) selection).getName();
                if (fieldName != null && fieldName.startsWith(INTROSPECTION_FIELD_PREFIX)) {
                    introspectionFields.add(fieldName);
                }
            }
        }
        return introspectionFields;
    }

    /**
     * Rejects a GraphQL request whose top-level selection set contains a blocked introspection field, with a
     * fault that accurately reflects what happened (introspection is not supported by the Gateway) instead of
     * the generic, REST-shaped "no matching resource" error the request would otherwise fall through to.
     *
     * @param messageContext      message context of the request
     * @param introspectionFields the introspection field name(s) that triggered the rejection
     */
    private void handleIntrospectionNotSupported(MessageContext messageContext, List<String> introspectionFields) {

        String apiContext = StringUtils.defaultIfBlank(
                (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT), "unknown");
        String apiVersion = StringUtils.defaultIfBlank(
                (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION), "unknown");
        String fieldList = String.join(",", introspectionFields);

        log.warn("GraphQL introspection request blocked for API: " + apiContext + ", version: " + apiVersion
                + " - requested introspection field(s): " + fieldList);

        messageContext.setProperty(SynapseConstants.ERROR_CODE,
                GraphQLConstants.GRAPHQL_INTROSPECTION_NOT_SUPPORTED);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE,
                GraphQLConstants.GRAPHQL_INTROSPECTION_NOT_SUPPORTED_MESSAGE);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL,
                "GraphQL introspection field(s) [" + fieldList + "] are not supported by the API Gateway. "
                        + "Introspection queries are not routed to the backend.");
        Mediator sequence = messageContext.getSequence(GraphQLConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_FORBIDDEN);
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



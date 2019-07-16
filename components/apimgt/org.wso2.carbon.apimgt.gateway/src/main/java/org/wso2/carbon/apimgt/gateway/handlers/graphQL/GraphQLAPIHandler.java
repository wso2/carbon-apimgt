package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.schema.GraphQLType;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.Entry;
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


import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

public class GraphQLAPIHandler extends AbstractHandler {

    public static final String QUERY_PATH_String = "/?query=";
    public static final String GRAPHQL_API_OPERATION_RESOURCE = "OPERATION_RESOURCE";
    public static final String GRAPHQL_SCOPE_ROLE_MAPPING = "GRAPHQL_SCOPE_ROLE_MAPPING";
    public static final String scopeRoleMapping = "ScopeRoleMapping";
    public static final String scopeOperationMapping = "ScopeOperationMapping";
    public static final String GRAPHQL_SCOPE_OPERATION_MAPPING = "GRAPHQL_SCOPE_OPERATION_MAPPING";
    public static final String GRAPHQL_API_OPERATION_TYPE = "OPERATION_TYPE";
    public static final String API_TYPE = "API_TYPE";
    public static final String GRAPHQL_API = "GRAPHQL";
    public static final int UNAUTHORIZED_REQUESRT = 401;
    private String apiUUID;

    public String getApiUUID() {
        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {
        this.apiUUID = apiUUID;
    }

    public boolean handleRequest(MessageContext messageContext) {
        try {
            OMElement omElement;
            String payload;
            String operationList = "";
            String validationErrorMessage = "";
            HashMap<String,String> rolelist = new HashMap<>();
            HashMap<String,String> operationlist = new HashMap<>();
            List<ValidationError> validationErrors = null;

            ArrayList<String> roleArray=new ArrayList<String>();
            ArrayList<String> operationArray=new ArrayList<String>();
            ArrayList<String> validationErrorMessageList =new ArrayList<String>();

            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();
            RelayUtils.buildMessage(axis2MC);

            if (axis2MC.getEnvelope().getBody().getFirstElement() != null) {
                payload = axis2MC.getEnvelope().getBody().getFirstElement().getFirstElement().getText();
            } else {
                String queryURLValue = ((Axis2MessageContext)messageContext).getProperties().
                        get("REST_SUB_REQUEST_PATH").toString().split(QUERY_PATH_String)[1];
                payload = URLDecoder.decode(queryURLValue, "UTF-8");
            }

            Parser parser = new Parser();
            Document document = parser.parseDocument(payload);
            SchemaParser schemaParser = new SchemaParser();
            GraphQLSchema schema = null;

            Entry localEntryObj = (Entry) messageContext.getConfiguration().getLocalRegistry().get(apiUUID + "_graphQL");
            if (localEntryObj != null) {
                TypeDefinitionRegistry registry = schemaParser.parse(localEntryObj.getValue().toString());
                schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
                Validator validator = new Validator();
                validationErrors = validator.validateDocument(schema, document);
            }
            if (validationErrors.size() > 0) {
                for(ValidationError error : validationErrors) {
                    validationErrorMessageList.add(error.getDescription());
                }
                validationErrorMessage = String.join(",", validationErrorMessageList);
                handleValidationFailure(messageContext, validationErrorMessage);
                return false;
            }

            Set<GraphQLType> additionalTypes = schema.getAdditionalTypes();
            for (GraphQLType  additionalType : additionalTypes) {
                if(additionalType.getName().contains(scopeRoleMapping)) {
                    String scope = additionalType.getName().split("_")[1];
                    additionalType.getChildren();
                    for (GraphQLType a :  additionalType.getChildren()) {
                        roleArray.add(a.getName());
                    }
                    rolelist.put(scope, String.join(",", roleArray));
                    messageContext.setProperty(GRAPHQL_SCOPE_ROLE_MAPPING, rolelist);
                } else if (additionalType.getName().contains(scopeOperationMapping)) {
                    String operation = additionalType.getName().split("_")[1];
                    for (GraphQLType a : additionalType.getChildren()) {
                        operationlist.put(operation, a.getName());
                    }
                    messageContext.setProperty(GRAPHQL_SCOPE_OPERATION_MAPPING, operationlist);
                }
            }

            messageContext.setProperty(API_TYPE, GRAPHQL_API);
            for (Definition definition : new Parser().parseDocument(payload)
                    .getDefinitions()) {
                if (definition instanceof OperationDefinition) {
                    OperationDefinition operation = (OperationDefinition) definition;
                    if (operation.getOperation() != null) {
                        messageContext.setProperty(GRAPHQL_API_OPERATION_TYPE, operation.getOperation());
                        if (operation.getOperation().equals(Operation.QUERY)) {
                            for (Selection selection : operation.getSelectionSet().getSelections()) {
                                if (selection instanceof Field) {
                                    Field field = (Field) selection;
                                    operationArray.add(field.getName());
                                    System.out.print("operation - Query " + field.getName());
                                }
                            }
                            Collections.sort(operationArray);
                            operationList = String.join(",", operationArray);
                        } else if (operation.getOperation().equals(Operation.MUTATION)) {
                            operationList = operation.getName();
                            System.out.print("operation - Mutation " + operation.getName());
                        } else if (operation.getOperation().equals(Operation.SUBSCRIPTION)) {
                            operationList = operation.getName();
                            System.out.print("operation - Subscription " + operation.getName());
                        }
                        messageContext.setProperty(GRAPHQL_API_OPERATION_RESOURCE, operationList);
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleValidationFailure(MessageContext messageContext, String validationErrorList) {
        OMElement payload = getFaultPayload(validationErrorList);
        setFaultPayload(messageContext, payload);
        sendFault(messageContext, UNAUTHORIZED_REQUESRT);
    }

    public static void setFaultPayload(MessageContext messageContext, OMElement payload) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        JsonUtil.removeJsonPayload(axis2MC);
        messageContext.getEnvelope().getBody().addChild(payload);
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String acceptType = (String) headers.get(HttpHeaders.ACCEPT);
        Set<String> supportedMimes = new HashSet<String>(Arrays.asList("application/x-www-form-urlencoded",
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
        if(!StringUtils.isEmpty(acceptType) && supportedMimes.contains(acceptType)){
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

    public static void sendFault(MessageContext messageContext, int status) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        axis2MC.setProperty(NhttpConstants.HTTP_SC, status);
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

    protected OMElement getFaultPayload(String list) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText("401");
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(list);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(list);

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


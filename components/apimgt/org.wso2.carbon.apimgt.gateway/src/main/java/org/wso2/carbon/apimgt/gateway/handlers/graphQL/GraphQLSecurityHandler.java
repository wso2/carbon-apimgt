package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.analysis.QueryDepthInfo;
import graphql.schema.GraphQLSchema;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.rest.AbstractHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.basicauth.BasicAuthCredentialValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.usermgt.APIKeyMgtRemoteUserClient;
import org.wso2.carbon.apimgt.gateway.handlers.security.usermgt.APIKeyMgtRemoteUserClientPool;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class GraphQLSecurityHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(GraphQLSecurityHandler.class);
    private APIKeyMgtRemoteUserClientPool clientPool = APIKeyMgtRemoteUserClientPool.getInstance();
    private GraphQLSchema schema = null;
    private static int MAX_QUERY_DEPTH = -1;
//    private APIKeyMgtRemoteUserStoreMgtServiceStub apiKeyMgtRemoteUserStoreMgtServiceStub;

    public GraphQLSecurityHandler() throws APISecurityException {
    }

//    /**
//     * Initializes this authenticator instance.
//     *
//     * @param env Current SynapseEnvironment instance
//     */
//    public void init(SynapseEnvironment env) {
//        try {
//            ConfigurationContext configurationContext = ServiceReferenceHolder.getInstance().getAxis2ConfigurationContext();
//            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
//            String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
//            String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
//            String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
//            if (url == null) {
//                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
//                        "API key manager URL unspecified");
//            }
//
//            try {
//                apiKeyMgtRemoteUserStoreMgtServiceStub = new APIKeyMgtRemoteUserStoreMgtServiceStub(configurationContext, url +
//                        "APIKeyMgtRemoteUserStoreMgtService");
//                ServiceClient client = apiKeyMgtRemoteUserStoreMgtServiceStub._getServiceClient();
//                Options options = client.getOptions();
//                options.setCallTransportCleanup(true);
//                options.setManageSession(true);
//                CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);
//            } catch (AxisFault axisFault) {
//                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, axisFault.getMessage(), axisFault);
//            }
////            this.basicAuthCredentialValidator = new BasicAuthCredentialValidator();
//        } catch (APISecurityException e) {
//            log.error(e);
//        }
//    }
//
//    @Override
//    public void destroy() {
//
//    }

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
     * This method returns the user roles
     *
     * @return list of user roles
     */
    private String[] getUserRoles() throws APISecurityException {
        String[] userRoles;
        APIKeyMgtRemoteUserClient client = null;
        try {
//            userRoles = apiKeyMgtRemoteUserStoreMgtServiceStub.getUserRoles(username);
            client = clientPool.get();
            userRoles = client.getUserRoles();
        } catch (APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException | RemoteException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        }
        return userRoles;
    }

    /**
     * This method returns the maximum query depth value
     *
     * @param userRoles list of user roles
     * @param policyDefinition object with role to depth mappings
     * @return maximum query depth value if exists, or -1 to denote no depth limitation
     */
    private int getMaxQueryDepth(String[] userRoles, JSONObject policyDefinition) {
        //
        try {
            Object complexityObject = policyDefinition.get("COMPLEXITY");
            Object testObject = policyDefinition.get("A");
        } catch (NullPointerException e) {
            log.error("Hello world");
        }
        //
        Object depthObject = policyDefinition.get("DEPTH");
        ArrayList<Integer> allocatedDepths = new ArrayList<Integer>();
        for (String role: userRoles) {
            try {
                int depth = ((Long)((JSONObject) depthObject).get(role)).intValue();
                allocatedDepths.add(depth);
            } catch (NullPointerException e) {
                if (log.isDebugEnabled()) {
                    log.debug("No depth limitation value was assigned for " +  role + " role");
                }
            }
        }
        if (allocatedDepths.size()==0) {
            try {
                int depth = ((Long)((JSONObject) depthObject).get("default")).intValue();
                return depth;
            } catch (NullPointerException e) {
                log.error("Test");
                return -1;
            }
//            return -1;
        } else {
            return Collections.max(allocatedDepths);
        }
    }

    /**
     * This method analyses the query
     *
     * @param messageContext message context of the request
     * @param payload payload of the request
     * @return true or false
     */
    private boolean analyseQuery(MessageContext messageContext, String payload) {
        if(queryDepthAnalysis(messageContext, payload)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method analyses the query depth
     *
     * @param messageContext message context of the request
     * @param payload payload of the request
     * @return true or false
     */
    private boolean queryDepthAnalysis(MessageContext messageContext, String payload) {
        JSONParser jsonParser = new JSONParser();
//        String username = APISecurityUtils.getAuthenticationContext(messageContext).getUsername();
        try {
            String[] userRoles = getUserRoles();
            String GraphQLAccessControlPolicy = (String) messageContext.getProperty(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY);
            JSONObject policyDefinition = (JSONObject) jsonParser.parse(GraphQLAccessControlPolicy);
            MAX_QUERY_DEPTH = getMaxQueryDepth(userRoles, policyDefinition);
            if (MAX_QUERY_DEPTH > 0) {

                MaxQueryDepthInstrumentation maxQueryDepthInstrumentation = new MaxQueryDepthInstrumentation(MAX_QUERY_DEPTH);

                GraphQL runtime = GraphQL.newGraphQL(schema)
                        .instrumentation(maxQueryDepthInstrumentation)
                        .build();

                try {
                    ExecutionResult executionResult = runtime.execute(payload);
                    List<GraphQLError> errors = executionResult.getErrors();
                    if (errors.size()>0) {
                        List<String> errorList = new ArrayList<>();
                        for (GraphQLError error : errors) {
                            errorList.add(error.getMessage());
                        }

                        ListIterator<String> iterator = errorList.listIterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().contains("non-nullable")) {
                                iterator.remove();
                            }
                        }

                        if (errorList.size()==0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Maximum query depth of " + MAX_QUERY_DEPTH + " was not exceeded");
                            }
                            return true;
                        }

                        handleFailure(messageContext, APISecurityConstants.QUERY_TOO_DEEP, errorList.toString());
                        log.error(errorList.toString());
                        return false;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Maximum query depth of " + MAX_QUERY_DEPTH + " was not exceeded");
                    }
                    return true;
                } catch (Throwable e) {
                    log.error(e);
                }

            } else {
                return true; // No depth limitation check
            }
        } catch (APISecurityException | ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method handle the failure
     *  @param messageContext message context of the request
     * @param errorMessage   error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleFailure(MessageContext messageContext, String errorMessage, String errorDescription) {
        OMElement payload = getFaultPayload(errorMessage, errorDescription);
        Utils.setFaultPayload(messageContext, payload);
        Mediator sequence = messageContext.getSequence(APISecurityConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    /**
     * @param message fault message
     * @return the OMElement
     */
    private OMElement getFaultPayload(String message, String description) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(APISecurityConstants.GRAPHQL_INVALID_QUERY + "");
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

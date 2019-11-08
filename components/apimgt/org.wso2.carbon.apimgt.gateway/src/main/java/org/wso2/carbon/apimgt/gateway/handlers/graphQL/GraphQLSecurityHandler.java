package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.analysis.MaxQueryDepthInstrumentation;
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
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.Cache;
import java.rmi.RemoteException;
import java.util.List;

public class GraphQLSecurityHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(GraphQLSecurityHandler.class);
    private GraphQLSchema schema = null;
    private static final int MAX_QUERY_DEPTH = 3;
    private APIKeyMgtRemoteUserStoreMgtServiceStub apiKeyMgtRemoteUserStoreMgtServiceStub;
    private boolean gatewayKeyCacheEnabled;

    GraphQLSecurityHandler() throws APISecurityException {
        ConfigurationContext configurationContext = ServiceReferenceHolder.getInstance().getAxis2ConfigurationContext();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        if (url == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "API key manager URL unspecified");
        }

        try {
            apiKeyMgtRemoteUserStoreMgtServiceStub = new APIKeyMgtRemoteUserStoreMgtServiceStub(configurationContext, url +
                    "APIKeyMgtRemoteUserStoreMgtService");
            ServiceClient client = apiKeyMgtRemoteUserStoreMgtServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setCallTransportCleanup(true);
            options.setManageSession(true);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);
        } catch (AxisFault axisFault) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, axisFault.getMessage(), axisFault);
        }
    }

    public boolean handleRequest(MessageContext messageContext) {
        String payload = messageContext.getProperty(APIConstants.GRAPHQL_PAYLOAD).toString();
        schema = (GraphQLSchema) messageContext.getProperty(APIConstants.GRAPHQL_SCHEMA);
        String username = APISecurityUtils.getAuthenticationContext(messageContext).getUsername();
        System.out.println(payload);
        System.out.println(schema);
        System.out.println(username);
        try {
            String[] userRoles = getUserRoles(username);
            System.out.println(userRoles);
        } catch (APISecurityException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String[] getUserRoles(String username) throws APISecurityException {
        String[] userRoles;
        try {
            userRoles = apiKeyMgtRemoteUserStoreMgtServiceStub.getUserRoles(username);
        } catch (APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException | RemoteException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        }
        return userRoles;
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
        MaxQueryDepthInstrumentation maxQueryDepthInstrumentation = new MaxQueryDepthInstrumentation(MAX_QUERY_DEPTH);

        GraphQL runtime = GraphQL.newGraphQL(schema)
                .instrumentation(maxQueryDepthInstrumentation)
                .build();

        try {
            ExecutionResult executionResult = runtime.execute(payload);
            List<GraphQLError> errors = executionResult.getErrors();
            if (errors.size()>0) {
                for (GraphQLError error : errors) {
                    log.error(error);
                }
                handleFailure(messageContext, APISecurityConstants.QUERY_TOO_COMPLEX, errors.toString());
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("Maximum query depth of " + MAX_QUERY_DEPTH + " was not exceeded");
            }
            return true;
        } catch (Throwable e) {
            log.error(e);
        }
        return false;
    }

    /**
     * This method handle the failure
     *
     * @param messageContext message context of the request
     * @param errorMessage   error message of the failure
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

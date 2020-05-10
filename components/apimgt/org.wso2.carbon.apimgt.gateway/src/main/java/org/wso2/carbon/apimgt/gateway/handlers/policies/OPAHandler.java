package org.wso2.carbon.apimgt.gateway.handlers.policies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.OPADto;

import org.json.simple.parser.ParseException;
//import org.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.tracing.Util;
//import org.wso2.carbon.apimgt.impl.utils.APIUtil;


import java.io.*;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.TreeMap;

public class OPAHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(OPAHandler.class);

    private String username;
    private String scopes;
    private String apiName;
    private String version;
    private String apiContext;
    private String resourcePath;
    private String httpMethod;
    private String apiType;
    private String applicationName;
    private String clientIp;

    OPADto opaDto;
    private static HttpClient httpClient = null;

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        setUsername(messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER).toString());
        setScopes(messageContext.getProperty("Access-Control-Allow-Headers").toString());
        setApiName(messageContext.getProperty(APIMgtGatewayConstants.API).toString());
        setVersion(messageContext.getProperty(APIMgtGatewayConstants.VERSION).toString());
        setApiContext(messageContext.getProperty(RESTConstants.REST_API_CONTEXT).toString());
        setResourcePath(messageContext.getProperty(APIMgtGatewayConstants.RESOURCE).toString());
        setHttpMethod(messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD).toString());
        setApiType(messageContext.getProperty(APIMgtGatewayConstants.API_TYPE).toString());
        setApplicationName(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME).toString());

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>)
                axis2MessageContext.getProperty
                        (org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (transportHeaderMap != null) {
            clientIp = transportHeaderMap.get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        }

        //Setting IP of the client
        if (clientIp != null && !clientIp.isEmpty()) {
            if (clientIp.indexOf(",") > 0) {
                clientIp = clientIp.substring(0, clientIp.indexOf(","));
            }
        } else {
            clientIp = (String) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }


        opaDto = new OPADto(
                getUsername(),
                getScopes().split(","),
                getApiName(),
                getVersion(),
                getApiContext(),
                getResourcePath(),
                getHttpMethod(),
                getApiType(),
                getApplicationName(),
                getClientIp()
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            String opaPayload = mapper.writeValueAsString(opaDto);
            opaPayload = "{\"input\" :" + opaPayload + "}";
            HttpResponse response = sendRequestToOPaServer(opaPayload);

            int statusCode = response.getStatusLine().getStatusCode();
            String finalAnswer = getResultFromOPAResponse(response);

            if(HttpStatus.SC_OK != statusCode){
                throw new RuntimeException("Failed with HTTP error code : "+ statusCode);
            }
            else if(HttpStatus.SC_OK == statusCode){
                if("true".equals(finalAnswer)){
                    return true;
                }
                else if("false".equals(finalAnswer)){
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,"OPA Failure");
                }
            }

        }
        catch (IOException e) {
            log.error("Could not get any response: The server couldn't send a response:\n" +
                    "Ensure that the backend is working properly\n" +
                    "Self-signed SSL certificates are being blocked:\n" +
                    "Fix this by turning off 'SSL certificate verification' in Settings > General\n" +
                    "Proxy configured incorrectly\n" +
                    "Ensure that proxy is configured correctly in Settings > Proxy\n" +
                    "Request timeout:\n" +
                    "Change request timeout in Settings > General",e);
        }
        catch (ParseException e) {
            log.error("Error while evaluating HTTP response",e);
        } catch (APISecurityException e) {
            handleOPAPolicyFailure(messageContext,e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return false;
    }

    private HttpResponse sendRequestToOPaServer(String jsonStr) throws IOException {
        httpClient = APIUtil.getHttpClient(8181,"http");
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Object serverEndpoint  =  configuration.getFirstProperty("APIGateway.Environments.Environment.OpaServer");

        HttpPost postRequest = new HttpPost(serverEndpoint.toString());
        postRequest.addHeader("Content-Type","application/json");
        StringEntity userEntity = new StringEntity(jsonStr, ContentType.APPLICATION_JSON);
        postRequest.setEntity(userEntity);

        return httpClient.execute(postRequest);
    }

    private String getResultFromOPAResponse(HttpResponse response) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String inputline ;
        StringBuilder responseString = new StringBuilder();

        while ((inputline = reader.readLine()) != null) {
            responseString.append(inputline);
        }
        reader.close();

        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
        Object object = responseJson.get("result");
        return object.toString();
    }

    private void handleOPAPolicyFailure(MessageContext messageContext, APISecurityException e) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, e.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE,
                APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION,e);

        String errorDetail = APISecurityConstants.getFailureMessageDetailDescription(e.getErrorCode(),e.getMessage());
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDetail);

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");

        int status = HttpStatus.SC_UNAUTHORIZED;
        Map<String, String> headers =
                (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            headers.put(HttpHeaders.WWW_AUTHENTICATE,
                    ", error=\"invalid_token\"" +
                    ", error_description=\"The access token expired\"");
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE,status);


        log.error("wrong credentials: Request breaks provided policies",e);
        Utils.setFaultPayload(messageContext, getFaultPayload(e));

        Utils.sendFault(messageContext, status);
    }

    protected OMElement getFaultPayload(APISecurityException e) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(e.getErrorCode()));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(APISecurityConstants.getFailureMessageDetailDescription(e.getErrorCode(), e.getMessage()));

        if (e.getErrorCode() == APISecurityConstants.API_AUTH_MISSING_CREDENTIALS) {
            String errorDescription = " Unauthenticated at backend level";
            errorDetail.setText(errorDescription);
        }

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    public static Log getLog() {
        return log;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}

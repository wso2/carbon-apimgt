package org.wso2.carbon.apimgt.gateway.handlers.policies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
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
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
//import org.wso2.carbon.apimgt.impl.utils.APIUtil;


import java.io.*;
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
                    handleOPAPolicyFailure(messageContext, HttpStatus.SC_UNAUTHORIZED );
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

    private boolean handleOPAPolicyFailure(MessageContext messageContext, int status)  {
        log.error("wrong credentials: Request breaks provided policies");
        Utils.sendFault(messageContext, status);
        return false;
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

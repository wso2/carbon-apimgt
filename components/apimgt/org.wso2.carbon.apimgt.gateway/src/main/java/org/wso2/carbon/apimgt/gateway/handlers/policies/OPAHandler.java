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
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.OPADto;

import org.json.simple.parser.ParseException;
//import org.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
//import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Properties;


import java.io.*;
import java.util.TreeMap;

public class OPAHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(OPAHandler.class);

    private String username;
    private String scopes;
    private String api_name;
    private String version;
    private String api_context;
    private String resource_path;
    private String http_method;
    private String api_type;
    private String application_name;
    private String client_ip;

    OPADto opaDto;
    HttpClient httpClient = APIUtil.getHttpClient(8181,"http");

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        setUsername(messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER).toString());
        setScopes(messageContext.getProperty("Access-Control-Allow-Headers").toString());
        setApi_name(messageContext.getProperty(APIMgtGatewayConstants.API).toString());
        setVersion(messageContext.getProperty(APIMgtGatewayConstants.VERSION).toString());
        setApi_context(messageContext.getProperty(RESTConstants.REST_API_CONTEXT).toString());
        setResource_path(messageContext.getProperty(APIMgtGatewayConstants.RESOURCE).toString());
        setHttp_method(messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD).toString());
        setApi_type(messageContext.getProperty(APIMgtGatewayConstants.API_TYPE).toString());
        setApplication_name(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME).toString());

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>)
                axis2MessageContext.getProperty
                        (org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (transportHeaderMap != null) {
            client_ip = transportHeaderMap.get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        }

        //Setting IP of the client
        if (client_ip != null && !client_ip.isEmpty()) {
            if (client_ip.indexOf(",") > 0) {
                client_ip = client_ip.substring(0, client_ip.indexOf(","));
            }
        } else {
            client_ip = (String) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }


        opaDto = new OPADto(
                getUsername(),
                getScopes().split(","),
                getApi_name(),
                getVersion(),
                getApi_context(),
                getResource_path(),
                getHttp_method(),
                getApi_type(),
                getApplication_name(),
                getClient_ip()
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
            log.error("Error occurred while reading the response",e);
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }

        return false;
    }

    private HttpResponse sendRequestToOPaServer(String jsonStr) throws IOException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Object server_url  =  configuration.getFirstProperty("APIGateway.Environments.Environment.OpaServer");

        HttpPost postRequest = new HttpPost(server_url.toString());
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

    private void handleOPAPolicyFailure(MessageContext messageContext, int status){
        log.error("wrong credentials: Request breaks provided policies");
        Utils.sendFault(messageContext, status);
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

    public String getApi_name() {
        return api_name;
    }

    public void setApi_name(String api_name) {
        this.api_name = api_name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApi_context() {
        return api_context;
    }

    public void setApi_context(String api_context) {
        this.api_context = api_context;
    }

    public String getResource_path() {
        return resource_path;
    }

    public void setResource_path(String resource_path) {
        this.resource_path = resource_path;
    }

    public String getHttp_method() {
        return http_method;
    }

    public void setHttp_method(String http_method) {
        this.http_method = http_method;
    }

    public String getApi_type() {
        return api_type;
    }

    public void setApi_type(String api_type) {
        this.api_type = api_type;
    }

    public String getApplication_name() {
        return application_name;
    }

    public void setApplication_name(String application_name) {
        this.application_name = application_name;
    }

    public String getClient_ip() {
        return client_ip;
    }

    public void setClient_ip(String client_ip) {
        this.client_ip = client_ip;
    }
}

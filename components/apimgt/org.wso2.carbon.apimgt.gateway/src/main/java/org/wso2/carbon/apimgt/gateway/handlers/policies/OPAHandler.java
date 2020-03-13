package org.wso2.carbon.apimgt.gateway.handlers.policies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.OPADto;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.*;

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

    OPADto opaDto;
    HttpClient httpClient = APIUtil.getHttpClient(8181,"http");

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        setUsername(messageContext.getProperty("tenant.info.id").toString());
        setScopes(messageContext.getProperty("Access-Control-Allow-Headers").toString());
        setApi_name(messageContext.getProperty(APIMgtGatewayConstants.API).toString());
        setVersion(messageContext.getProperty(APIMgtGatewayConstants.VERSION).toString());
        setApi_context(messageContext.getProperty("REST_API_CONTEXT").toString());
        setResource_path(messageContext.getProperty("API_RESOURCE_CACHE_KEY").toString());
        setHttp_method(messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD).toString());
        setApi_type(messageContext.getProperty(APIMgtGatewayConstants.API_TYPE).toString());
        setApplication_name(messageContext.getProperty("ARTIFACT_NAME").toString());

        opaDto = new OPADto(
                getUsername(),
                getScopes().split(","),
                getApi_name(),
                getVersion(),
                getApi_context(),
                getResource_path(),
                getHttp_method(),
                getApi_type(),
                getApplication_name()
        );

       try {
           ObjectMapper mapper = new ObjectMapper();
           String jsonStr = mapper.writeValueAsString(opaDto);

           HttpResponse response = sendRequestToOPaServer(jsonStr);
           int statusCode = response.getStatusLine().getStatusCode();
           String finalAnswer = getResultFromOPAResponse(response);

           if(statusCode != HttpStatus.SC_OK){
               throw new RuntimeException("Failed with HTTP error code : "+ statusCode);
           }
           if(finalAnswer.equals("true")){
               return true;
           }

       } catch (UnsupportedEncodingException e) {
           e.printStackTrace();
       } catch (ClientProtocolException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       } catch (ParseException e) {
           e.printStackTrace();
       } finally {
           httpClient.getConnectionManager().shutdown();
       }

       return false;
    }

    private HttpResponse sendRequestToOPaServer(String jsonStr) throws IOException {
        HttpPost postRequest = new HttpPost("http://0.0.0.0:8181/v1/data/opa/examples/allow");
        postRequest.addHeader("content-type", "application/xml");
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
        String report = responseJson.toString();

        return report.substring(10,report.length()-1);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return false;
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



}

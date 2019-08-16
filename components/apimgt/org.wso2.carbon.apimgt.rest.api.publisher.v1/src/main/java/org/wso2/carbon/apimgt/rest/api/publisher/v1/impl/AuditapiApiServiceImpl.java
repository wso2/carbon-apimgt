package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APISecurityAuditInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SecurityAuditAPIIDDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class AuditapiApiServiceImpl implements AuditapiApiService {

    private static final Log log = LogFactory.getLog(AuditapiApiServiceImpl.class);

    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_API_TOKEN = "X-API-KEY";
    private static final String HEADER_APPLICATION_JSON = "application/json";

    public Response auditapiGet(String apiId, String accept, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    public Response auditapiPost(String apiId, APISecurityAuditInfoDTO body, String accept, MessageContext messageContext) {
          boolean isDebugEnabled = log.isDebugEnabled();
          try {
              String username = RestApiUtil.getLoggedInUsername();
              String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
              APIProvider apiProvider = RestApiUtil.getProvider(username);
              API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
              APIIdentifier apiIdentifier = api.getId();
              String apiDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier);

              // Get configuration file and retrieve API token and Collection ID
              APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                      .getAPIManagerConfigurationService().getAPIManagerConfiguration();
              String apiToken = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN);
              String collectionId = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_CID);

              // Initiate JSON parser.
              JSONParser parser = new JSONParser();
              JSONObject jsonObject;

              // Parse JSON String of API Definition
              jsonObject = (JSONObject) parser.parse(apiDefinition);

              // Set properties to be attached in the body of the request
              body.setName(apiIdentifier.getApiName());
              body.setCid(collectionId);
              body.setSpecfile(jsonObject);

              // Logic for HTTP Request
              URL auditUrl = new URL("https://platform.42crunch.com/api/v1/apis");
              try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil.getHttpClient(auditUrl.getPort(), auditUrl.getProtocol())) {
                  HttpPost httpPost = new HttpPost(String.valueOf(auditUrl));

                  // Set the header properties of the request
                  httpPost.setHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
                  httpPost.setHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
                  httpPost.setHeader(HEADER_API_TOKEN, apiToken);
                  httpPost.setEntity(new StringEntity(body.toString()));

                  // Code block for the processing of the response
                  try(CloseableHttpResponse response = httpClient.execute(httpPost)) {
                      if (isDebugEnabled) {
                          log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                      }
                      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                          BufferedReader reader = new BufferedReader(
                                  new InputStreamReader(response.getEntity().getContent()));
                          String inputLine;
                          StringBuilder responseString = new StringBuilder();

                          while((inputLine = reader.readLine()) != null) {
                              responseString.append(inputLine);
                          }
                          JSONObject responseObject;
                          responseObject = (JSONObject) parser.parse(responseString.toString());
                          String newAuditAPIId = (String)((JSONObject) responseObject.get("desc")).get("id");
                          // TODO - Create a method in APIMgtDAO class to store variable in db table and call it here to pass to the db.

                          return Response.ok().entity(responseString.toString()).build();
                      } else {
                          throw new APIManagementException(
                                  "Error while retrieving data from " + auditUrl + ". Found http status " + response
                                  .getStatusLine());
                      }
                  } finally {
                      httpPost.releaseConnection();
                  }
              } catch (IOException e) {
                  log.error("Error occurred while getting HttpClient instance");
              }
          } catch (APIManagementException e) {
              String errorMessage = "Error while creating new Audit API : " + apiId;
              RestApiUtil.handleInternalServerError(errorMessage, e, log);
          } catch (ParseException e) {
              log.error("API Definition String could not be parsed into JSONObject.");
          }
          return null;
    }

    public Response auditapiPut(String apiId, String accept, MessageContext messageContext) {
        boolean isDebugEnabled = log.isDebugEnabled();

        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = api.getId();
            String apiDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier);

            // Get configuration file and retrieve API token and Collection ID
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String apiToken = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN);
            String collectionId = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_CID);

            // Initiate JSON Parser
            JSONParser parser = new JSONParser();
            JSONObject jsonObject;

            // Parse JSON String of API Definition
            jsonObject = (JSONObject) parser.parse(apiDefinition);

            // Set the property to be attached in the body of the request

        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Audit API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            log.error("API Definition String could not be parsed into JSONObject");
        }
        return Response.ok().entity("magic!").build();
    }
}

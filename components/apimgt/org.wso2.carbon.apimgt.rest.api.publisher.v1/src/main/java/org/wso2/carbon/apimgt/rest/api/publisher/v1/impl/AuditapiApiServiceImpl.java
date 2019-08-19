package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APISecurityAuditInfoDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.Response;


public class AuditapiApiServiceImpl implements AuditapiApiService {

    private static final Log log = LogFactory.getLog(AuditapiApiServiceImpl.class);

    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_API_TOKEN = "X-API-KEY";
    private static final String HEADER_APPLICATION_JSON = "application/json";

    public Response auditapiGet(String apiId, String accept, MessageContext messageContext) {
        boolean isDebugEnabled = log.isDebugEnabled();

        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = api.getId();

            // Get configuration file and retrieve API token
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String apiToken = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN);

            // Retrieve the uuid from the database
            String uuid = ApiMgtDAO.getInstance().getAuditApiId(apiIdentifier);

            // Logic for the HTTP request
            URL auditURL = new URL("https://platform.42crunch.com/api/v1/apis/" + uuid + "/assessmentreport");
            try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil.getHttpClient(auditURL.getPort(), auditURL.getProtocol())) {
                HttpGet httpGet = new HttpGet(String.valueOf(auditURL));

                // Set the header properties of the request
                httpGet.setHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
                httpGet.setHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
                httpGet.setHeader(HEADER_API_TOKEN, apiToken);

                // Code block for the processing of the response
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    if (isDebugEnabled) {
                        log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                    }
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getEntity().getContent()));
                        String inputLine;
                        StringBuilder responseString = new StringBuilder();

                        while ((inputLine = reader.readLine()) != null) {
                            responseString.append(inputLine);
                        }
                        return Response.ok().entity(responseString.toString()).build();
                    }
                }
            } catch (IOException e) {
                log.error("Error occurred while getting HttpClient instance");
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Security Audit Report for API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
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

                  // Construct the JSON String to be passed in the request
                  StringBuilder bodyString = new StringBuilder();
                  bodyString.append("{ \n");
                  bodyString.append("   \"specfile\": ").append(body.getSpecfile()).append("\n");
                  bodyString.append("   \"cid\": ").append(body.getCid()).append("\n");
                  bodyString.append("   \"name\": ").append(body.getName()).append("\n");
                  bodyString.append("}");

                  // Set the header properties of the request
                  httpPost.setHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
                  httpPost.setHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
                  httpPost.setHeader(HEADER_API_TOKEN, apiToken);
                  httpPost.setEntity(new StringEntity(bodyString.toString()));

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
                          ApiMgtDAO.getInstance().addAuditApiMapping(apiIdentifier, newAuditAPIId);

                          return Response.ok().entity(newAuditAPIId).build();
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

            // Get configuration file and retrieve API token
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String apiToken = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN);

            // Initiate JSON Parser
            JSONParser parser = new JSONParser();
            JSONObject jsonObject;

            // Parse JSON String of API Definition
            jsonObject = (JSONObject) parser.parse(apiDefinition);

            // Set the property to be attached in the body of the request
            // Attach API Definition to property called specfile to be sent in the request
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{\n");
            stringBuilder.append("  \"specfile\":   ").append(jsonObject).append("\n");
            stringBuilder.append("}");

            // Retrieve the uuid from the database
            String uuid = ApiMgtDAO.getInstance().getAuditApiId(apiIdentifier);

            // Logic for HTTP Request
            URL auditURL = new URL("https://platform.42crunch.com/api/v1/apis/" + uuid);
            try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil.getHttpClient(auditURL.getPort(), auditURL.getProtocol())) {
                HttpPut httpPut = new HttpPut(String.valueOf(auditURL));

                // Set the header properties of the request
                httpPut.setHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
                httpPut.setHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
                httpPut.setHeader(HEADER_API_TOKEN, apiToken);
                httpPut.setEntity(new StringEntity(stringBuilder.toString()));

                // Code block for processing the response
                try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                    if (isDebugEnabled) {
                        log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                    }
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getEntity().getContent()));
                        String inputLine;
                        StringBuilder responseString = new StringBuilder();

                        while ((inputLine = reader.readLine()) != null) {
                            responseString.append(inputLine);
                        }

                        return Response.ok().entity(responseString.toString()).build();
                    } else {
                        throw new APIManagementException("Error while sending data to " + auditURL +
                                ". Found http status " + response.getStatusLine());
                    }
                } finally {
                    httpPut.releaseConnection();
                }
            } catch (IOException e) {
                log.error("Error occurred while getting HttpClient instance");
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Audit API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            log.error("API Definition String could not be parsed into JSONObject");
        }
        return null;
    }
}

/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;

import java.io.IOException;

/**
 * The Key Manager implementation for scenarios where WSO2 IS is used as the authorization server.
 */
public class WSO2ISKeyManagerImpl extends AMDefaultKeyManagerImpl implements KeyManager {

    private static final Log log = LogFactory.getLog(WSO2ISKeyManagerImpl.class);

    /**
     * This method will be used to register a service provider application in the authorization server for the given
     * tenant. This method register the service provider application in WSO2 IS by calling the application management
     * rest apis. The cross-tenant should be true for the relevant resource in identity.xml of WSO2 IS.
     *
     * @param tenantDomain tenant domain to register the application
     * @throws APIManagementException if an error occurs while registering application
     */
    @Override
    public OAuthApplicationInfo registerKeyManagerMgtApplication(String tenantDomain) throws APIManagementException {

        String clientName = APIConstants.KEY_MANAGER_CLIENT_APPLICATION_PREFIX + tenantDomain;
        String authServerURL = getKeyManagerConfiguration().getParameter(APIConstants.AUTHSERVER_URL);
        if (StringUtils.isEmpty(authServerURL)) {
            throw new APIManagementException("API Key Validator Server URL cannot be empty or null");
        }
        String appMgtEndpoint = getAppManagementServiceEndpoint(tenantDomain);
        try {
            HttpPost httpPost = new HttpPost(appMgtEndpoint);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthorizationHeader());
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            // Create Application payload
            JSONArray grantTypes = new JSONArray();
            grantTypes.add(APIConstants.GRANT_TYPE_CLIENT_CREDENTIALS);
            JSONObject oidc = new JSONObject();
            oidc.put(APIConstants.WSO2_IS_APP_INBOUND_OIDC_GRANT_TYPES, grantTypes);
            JSONObject inboundConfig = new JSONObject();
            inboundConfig.put(APIConstants.WSO2_IS_APP_INBOUND_OIDC, oidc);
            JSONObject appPayload = new JSONObject();
            appPayload.put(APIConstants.WSO2_IS_APP_NAME, clientName);
            appPayload.put(APIConstants.WSO2_IS_APP_INBOUND_CONFIG, inboundConfig);

            StringEntity payload = new StringEntity(appPayload.toJSONString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(payload);
            if (log.isDebugEnabled()) {
                log.debug("Invoking Application Management REST API of WSO2 IS: " + appMgtEndpoint + " to register " +
                        "application " + clientName);
            }
            try (CloseableHttpResponse httpResponse = getKmHttpClient().execute(httpPost)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String responseString = readHttpResponseAsString(httpResponse);
                if (statusCode == HttpStatus.SC_CREATED) {
                    // The application id returned in the location header as a URL to the authorization server
                    Header locationHeader = httpResponse.getFirstHeader(HttpHeaders.LOCATION);
                    if (locationHeader != null) {
                        // Get the inbound configuration of the return application to get the client credentials of
                        // the relavent oauth app.
                        return getInboundConfiguration(locationHeader.getValue(), clientName);
                    }
                }
                throw new APIManagementException("Error occurred while registering application: " + clientName + " "
                        + "via " + appMgtEndpoint + ". Error Status: " + statusCode + " . Error Response: "
                        + responseString);
            }
        } catch (IOException e) {
            String errorMessage =
                    "Error occurred while registering application: " + clientName + " via " + appMgtEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * Get the oauth app inbound configuration.
     *
     * @param applicationEndpoint The application endpoint URL
     * @param clientName          Client Application name
     * @return OAuthApplicationInfo with the client credentials of the created application
     * @throws APIManagementException If an error occurs while getting inbound configuration
     */
    private OAuthApplicationInfo getInboundConfiguration(String applicationEndpoint, String clientName)
            throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo;
        String appMgtEndpoint = applicationEndpoint + APIConstants.WSO2_IS_APP_MGT_INBOUND_OIDC_SUFFIX;
        HttpGet httpGet = new HttpGet(appMgtEndpoint);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthorizationHeader());
        if (log.isDebugEnabled()) {
            log.debug("Invoking App Management REST API of WSO2 IS: " + appMgtEndpoint + " to get "
                    + "inbound configuration of app: " + clientName);
        }
        try (CloseableHttpResponse httpAppResponse = getKmHttpClient().execute(httpGet)) {
            int statusCode = httpAppResponse.getStatusLine().getStatusCode();
            String responseString = readHttpResponseAsString(httpAppResponse);
            if (statusCode == HttpStatus.SC_OK && StringUtils.isNoneEmpty(responseString)) {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseString);
                String clientId =
                        (String) obj.get(APIConstants.WSO2_IS_APP_CLIENT_ID);
                String clientSecret =
                        (String) obj.get(APIConstants.WSO2_IS_APP_CLIENT_SECRET);
                oAuthApplicationInfo = new OAuthApplicationInfo();
                oAuthApplicationInfo.setClientId(clientId);
                oAuthApplicationInfo.setClientSecret(clientSecret);
                oAuthApplicationInfo.setClientName(clientName);
                return oAuthApplicationInfo;
            }
            throw new APIManagementException("Error occurred while retrieving inbound config for:"
                    + clientName + " via " + appMgtEndpoint + ". Error Status: " + statusCode
                    + ". Error Response: " + responseString);
        } catch (IOException | ParseException e) {
            String errorMessage = "Error occurred while retrieving inbound config for:"
                    + clientName + " via " + appMgtEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * Get application management service tenant URL for given WSO2 IS endpoint.
     *
     * @param tenantDomain Tenant domain
     * @return Scope Management Service host URL (Eg:https://localhost:9443/t/carbon.super/api/server/v1/applications)
     * @throws APIManagementException If a malformed WSO2 IS endpoint is provided
     */
    private String getAppManagementServiceEndpoint(String tenantDomain) throws APIManagementException {

        String authServerURL = getKeyManagerConfiguration().getParameter(APIConstants.AUTHSERVER_URL);
        if (StringUtils.isEmpty(authServerURL)) {
            throw new APIManagementException("API Key Validator Server URL cannot be empty or null");
        }
        String appMgtTenantEndpoint = authServerURL.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0];
        if (StringUtils.isNoneEmpty(tenantDomain)) {
            appMgtTenantEndpoint += APIConstants.TENANT_PREFIX + tenantDomain;
        }
        appMgtTenantEndpoint += APIConstants.WSO2_IS_APP_MGT_REST_API_BASE_PATH;
        return appMgtTenantEndpoint;
    }
}

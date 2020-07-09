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
package org.wso2.carbon.apimgt.impl.service;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.KMRegisterProfileDTO;
import org.wso2.carbon.apimgt.impl.dto.TokenHandlingDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class is responsible for calling the KM management services in WSO2 API-M KeyManager Profile server.
 */
public final class KeyMgtRegistrationService {

    private static final Log log = LogFactory.getLog(KeyMgtRegistrationService.class);

    private KeyMgtRegistrationService() {

        throw new IllegalStateException("Service class for key manager registration");
    }

    /**
     * This method will be used to register a service provider application in the authorization server for the given
     * tenant.
     *
     * @param tenantDomain tenant domain to register the application
     * @return OAuthApplicationInfo object with clientId, clientSecret and client name of the registered OAuth app
     * @throws APIManagementException if an error occurs while registering application
     */
    private static OAuthApplicationInfo registerKeyMgtApplication(String tenantDomain, String keyManagerServiceUrl,
                                                                  String username, String password)
            throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo = null;
        String clientName = APIConstants.KEY_MANAGER_CLIENT_APPLICATION_PREFIX + tenantDomain;
        if (StringUtils.isEmpty(keyManagerServiceUrl)) {
            throw new APIManagementException("API Key Validator Server URL cannot be empty or null");
        }
        String dcrEndpoint = keyManagerServiceUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0];
        dcrEndpoint += APIConstants.RestApiConstants.DYNAMIC_CLIENT_REGISTRATION_URL_SUFFIX;
        try {
            HttpPost httpPost = new HttpPost(dcrEndpoint);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthorizationHeader(username, password));
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            // Create DCR request payload
            KMRegisterProfileDTO kmRegisterProfileDTO = new KMRegisterProfileDTO();
            kmRegisterProfileDTO.setClientName(clientName);
            kmRegisterProfileDTO.setOwner(APIUtil.getTenantAdminUserName(tenantDomain));
            kmRegisterProfileDTO.setGrantType(APIConstants.GRANT_TYPE_CLIENT_CREDENTIALS);
            StringEntity payload = new StringEntity(new Gson().toJson(kmRegisterProfileDTO));
            httpPost.setEntity(payload);
            if (log.isDebugEnabled()) {
                log.debug("Invoking DCR REST API of KM: " + dcrEndpoint + " to register application " + clientName);
            }
            java.net.URL keyManagerURL = new java.net.URL(keyManagerServiceUrl);
            int keyManagerPort = keyManagerURL.getPort();
            String keyManagerProtocol = keyManagerURL.getProtocol();

            try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil
                    .getHttpClient(keyManagerPort, keyManagerProtocol)) {
                try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                            String responseContent = IOUtils.toString(inputStream);
                            if (StringUtils.isNotEmpty(responseContent)){
                                oAuthApplicationInfo = new Gson().fromJson(responseContent, OAuthApplicationInfo.class);
                            }
                        }
                    } else {
                        throw new APIManagementException("Error occurred while registering application: " + clientName + " "
                                + "via " + dcrEndpoint + ". Error Status: " + statusCode );
                    }
                }
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while registering application: " + clientName + " via " + dcrEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
        return oAuthApplicationInfo;
    }

    /**
     * Get Basic Authorization header for KM admin credentials.
     *
     * @return Base64 encoded Basic Authorization header
     */
    private static String getBasicAuthorizationHeader(String username, String password) {

        //Set Authorization Header of external store admin
        byte[] encodedAuth = Base64.encodeBase64((username + ":" + password).getBytes(StandardCharsets.ISO_8859_1));
        return APIConstants.AUTHORIZATION_HEADER_BASIC + StringUtils.SPACE + new String(encodedAuth);
    }

    public static void registerDefaultKeyManager(String tenantDomain) throws APIManagementException {

        synchronized (KeyMgtRegistrationService.class.getName().concat(tenantDomain)) {
            ApiMgtDAO instance = ApiMgtDAO.getInstance();
            if (instance.getKeyManagerConfigurationByName(tenantDomain, APIConstants.KeyManager.DEFAULT_KEY_MANAGER) ==
                    null) {

                APIManagerConfigurationService apiManagerConfigurationService =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();

                KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                keyManagerConfigurationDTO.setName(APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
                keyManagerConfigurationDTO.setEnabled(true);
                keyManagerConfigurationDTO.setUuid(UUID.randomUUID().toString());
                keyManagerConfigurationDTO.setTenantDomain(tenantDomain);
                keyManagerConfigurationDTO.setType(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE);
                keyManagerConfigurationDTO.setDescription(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_DESCRIPTION);
                if (apiManagerConfigurationService != null &&
                        apiManagerConfigurationService.getAPIManagerConfiguration() != null) {
                    String username = apiManagerConfigurationService.getAPIManagerConfiguration()
                            .getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
                    String password = apiManagerConfigurationService.getAPIManagerConfiguration()
                            .getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
                    String serviceURl = apiManagerConfigurationService.getAPIManagerConfiguration()
                            .getFirstProperty(APIConstants.KEYMANAGER_SERVERURL);
                    OAuthApplicationInfo oAuthApplicationInfo =
                            registerKeyMgtApplication(tenantDomain, serviceURl, username, password);
                    if (oAuthApplicationInfo != null) {
                        keyManagerConfigurationDTO.addProperty(APIConstants.KEY_MANAGER_CONSUMER_KEY,
                                oAuthApplicationInfo.getClientId());
                        keyManagerConfigurationDTO.addProperty(APIConstants.KEY_MANAGER_CONSUMER_SECRET,
                                oAuthApplicationInfo.getClientSecret());
                    }
                }
                TokenHandlingDto tokenHandlingDto = new TokenHandlingDto();
                tokenHandlingDto.setEnable(true);
                tokenHandlingDto.setType(TokenHandlingDto.TypeEnum.REFERENCE);
                tokenHandlingDto.setValue(APIConstants.KeyManager.UUID_REGEX);
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                        new Gson().toJson(Arrays.asList(tokenHandlingDto)));
                instance.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            }
        }
    }
}

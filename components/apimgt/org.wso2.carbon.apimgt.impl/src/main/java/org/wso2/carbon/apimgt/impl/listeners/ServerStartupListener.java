/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.listeners;

import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtDAOException;
import org.wso2.carbon.apimgt.api.APIMgtInternalException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.SystemApplicationDTO;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.loader.KeyManagerConfigurationDataRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.apimgt.impl.dao.SystemApplicationDAO;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for performing operations on initial server startup
 */
public class ServerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completedServerStartup() {

        //Copy extensible identity jsp files to the relevant extensions folder
        copyToExtensions();

        //Create Service Providers for Admin Publisher and Devportal web apps for the first time during server startup
        try {
            createSpsForPortalApps();
        } catch (APIManagementException e) {
            log.error("Error creating Service Providers for Portal Web Applications", e);
        }

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String enableKeyManagerRetrieval =
                    apiManagerConfiguration.getFirstProperty(APIConstants.ENABLE_KEY_MANAGER_RETRIVAL);
            if (JavaUtils.isTrueExplicitly(enableKeyManagerRetrieval)) {
                startConfigureKeyManagerConfigurations();
            }
            Map<String, TokenIssuerDto> tokenIssuerDtoMap =
                    apiManagerConfiguration.getJwtConfigurationDto().getTokenIssuerDtoMap();
            tokenIssuerDtoMap.forEach((issuer, tokenIssuer) -> KeyManagerHolder.addGlobalJWTValidators(tokenIssuer));
        }
    }

    /**
     * Method for copying identity component jsp pages to webapp extensions upon initial server startup
     */
    private static void copyToExtensions() {
        String repositoryDir = "repository";
        String resourcesDir = "resources";
        String extensionsDir = "extensions";
        String customAssetsDir = "customAssets";
        String webappDir = "webapps";
        String authenticationEndpointDir = "authenticationendpoint";
        String accountRecoveryEndpointDir = "accountrecoveryendpoint";
        String headerJspFile = "header.jsp";
        String footerJspFile = "product-footer.jsp";
        String titleJspFile = "product-title.jsp";
        String cookiePolicyContentJspFile = "cookie-policy-content.jsp";
        String privacyPolicyContentJspFile = "privacy-policy-content.jsp";
        try {
            String resourceExtDirectoryPath =
                    CarbonUtils.getCarbonHome() + File.separator + repositoryDir + File.separator + resourcesDir
                            + File.separator + extensionsDir;
            String customAssetsExtDirectoryPath =
                    CarbonUtils.getCarbonHome() + File.separator + repositoryDir + File.separator + resourcesDir
                    + File.separator + extensionsDir + File.separator + customAssetsDir;
            String authenticationEndpointWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + authenticationEndpointDir;
            String authenticationEndpointWebAppExtPath =
                    authenticationEndpointWebAppPath + File.separator + extensionsDir;
            String accountRecoveryWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + accountRecoveryEndpointDir;
            String accountRecoveryWebAppExtPath = accountRecoveryWebAppPath + File.separator + extensionsDir;
            if (new File(resourceExtDirectoryPath).exists()) {
                // delete extensions directory from the webapp folders if they exist
                FileUtils.deleteDirectory(new File(authenticationEndpointWebAppExtPath));
                FileUtils.deleteDirectory(new File(accountRecoveryWebAppExtPath));
                log.info("Starting to copy identity page extensions...");
                String headerJsp = resourceExtDirectoryPath + File.separator + headerJspFile;
                String footerJsp = resourceExtDirectoryPath + File.separator + footerJspFile;
                String titleJsp = resourceExtDirectoryPath + File.separator + titleJspFile;
                String cookiePolicyContentJsp = resourceExtDirectoryPath + File.separator + cookiePolicyContentJspFile;
                String privacyPolicyContentJsp =
                        resourceExtDirectoryPath + File.separator + privacyPolicyContentJspFile;
                if (new File(headerJsp).exists()) {
                    copyFileToDirectory(headerJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(headerJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(footerJsp).exists()) {
                    copyFileToDirectory(footerJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(footerJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(titleJsp).exists()) {
                    copyFileToDirectory(titleJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(titleJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(cookiePolicyContentJsp).exists()) {
                    copyFileToDirectory(cookiePolicyContentJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                }
                if (new File(privacyPolicyContentJsp).exists()) {
                    copyFileToDirectory(privacyPolicyContentJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                }
                // copy custom asset files to the webapp directories
                if (new File(customAssetsExtDirectoryPath).exists()) {
                    FileUtils.copyDirectory(new File(customAssetsExtDirectoryPath),
                            new File(authenticationEndpointWebAppExtPath + File.separator + customAssetsDir));
                    FileUtils.copyDirectory(new File(customAssetsExtDirectoryPath),
                            new File(accountRecoveryWebAppExtPath + File.separator + customAssetsDir));
                }
                log.info("Successfully completed copying identity page extensions");
            }
        } catch (IOException ex) {
            log.error("An error occurred while copying extension files to web apps", ex);
        }
    }

    private static void copyFileToDirectory(String filePath, String directoryPath, String parentDir)
            throws IOException {
        try {
            if (new File(parentDir).exists()) {
                FileUtils.copyFileToDirectory(new File(filePath), new File(directoryPath));
            }
        } catch (IOException ex) {
            log.error("An error occurred while copying file to directory", ex);
            throw new IOException("An error occurred while copying file to directory", ex);
        }
    }

    private void startConfigureKeyManagerConfigurations() {

        KeyManagerConfigurationDataRetriever keyManagerConfigurationDataRetriever =
                new KeyManagerConfigurationDataRetriever(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        keyManagerConfigurationDataRetriever.startLoadKeyManagerConfigurations();
    }

    /**
     * Method for creating service provider applications for the Publisher, Dev portal and Admin portals
     * upon initial server startup
     */
    private static void createSpsForPortalApps() throws APIManagementException {
        SystemApplicationDAO systemApplicationDAO = new SystemApplicationDAO();
        SystemApplicationDTO systemApplicationDTOAdmin;
        SystemApplicationDTO systemApplicationDTOPublisher;
        SystemApplicationDTO systemApplicationDTODevportal;

        String dcrUrl = getLoopbackOrigin() + APIConstants.ServerStartupListenerConstants.DCR_URL_SUFFIX;
        String serverUrl;
        try {
            serverUrl = APIUtil.getServerURL();
        } catch (APIManagementException e) {
            String errorMsg = "Error getting Server Url";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }

        try {
            systemApplicationDTOAdmin = systemApplicationDAO.getClientCredentialsForApplication(
                    APIConstants.ServerStartupListenerConstants.ADMIN_CLIENT_APP_NAME,
                    APIConstants.ServerStartupListenerConstants.SUPER_TENANT_DOMAIN);
            systemApplicationDTOPublisher = systemApplicationDAO.getClientCredentialsForApplication(
                    APIConstants.ServerStartupListenerConstants.PUBLISHER_CLIENT_APP_NAME,
                    APIConstants.ServerStartupListenerConstants.SUPER_TENANT_DOMAIN);
            systemApplicationDTODevportal = systemApplicationDAO.getClientCredentialsForApplication(
                    APIConstants.ServerStartupListenerConstants.DEVPORTAL_CLIENT_APP_NAME,
                    APIConstants.ServerStartupListenerConstants.SUPER_TENANT_DOMAIN);

            if (systemApplicationDTOAdmin == null) {
                String callbackUrl = generateCallbackUrl(APIConstants.ServerStartupListenerConstants.ADMIN_APP_CONTEXT,
                        serverUrl);
                Map<String, String> response = sendDCRRequest(
                        APIConstants.ServerStartupListenerConstants.ADMIN_CLIENT_APP_NAME, callbackUrl, dcrUrl);
                //persist the new app in the system apps table
                systemApplicationDAO
                        .addApplicationKey(APIConstants.ServerStartupListenerConstants.ADMIN_CLIENT_APP_NAME,
                                response.get(APIConstants.ServerStartupListenerConstants.CLIENT_ID),
                                response.get(APIConstants.ServerStartupListenerConstants.CLIENT_SECRET),
                                APIConstants.ServerStartupListenerConstants.SUPER_TENANT_DOMAIN);
                log.info("Successfully registered service provider for "
                        + APIConstants.ServerStartupListenerConstants.ADMIN_CLIENT_APP_NAME);
            }
            if (systemApplicationDTOPublisher == null) {
                String callbackUrl = generateCallbackUrl(
                        APIConstants.ServerStartupListenerConstants.PUBLISHER_APP_CONTEXT, serverUrl);
                Map<String, String> response = sendDCRRequest(
                        APIConstants.ServerStartupListenerConstants.PUBLISHER_CLIENT_APP_NAME, callbackUrl, dcrUrl);
                //persist the new app in the system apps table
                systemApplicationDAO
                        .addApplicationKey(APIConstants.ServerStartupListenerConstants.PUBLISHER_CLIENT_APP_NAME,
                                response.get(APIConstants.ServerStartupListenerConstants.CLIENT_ID),
                                response.get(APIConstants.ServerStartupListenerConstants.CLIENT_SECRET),
                                APIConstants.ServerStartupListenerConstants.SUPER_TENANT_DOMAIN);
                log.info("Successfully registered service provider for "
                        + APIConstants.ServerStartupListenerConstants.PUBLISHER_CLIENT_APP_NAME);
            }
            if (systemApplicationDTODevportal == null) {
                String callbackUrl = generateCallbackUrl(
                        APIConstants.ServerStartupListenerConstants.DEVPORTAL_APP_CONTEXT, serverUrl);
                Map<String, String> response = sendDCRRequest(
                        APIConstants.ServerStartupListenerConstants.DEVPORTAL_CLIENT_APP_NAME, callbackUrl, dcrUrl);
                //persist the new app in the system apps table
                systemApplicationDAO
                        .addApplicationKey(APIConstants.ServerStartupListenerConstants.DEVPORTAL_CLIENT_APP_NAME,
                                response.get(APIConstants.ServerStartupListenerConstants.CLIENT_ID),
                                response.get(APIConstants.ServerStartupListenerConstants.CLIENT_SECRET),
                                APIConstants.ServerStartupListenerConstants.SUPER_TENANT_DOMAIN);
                log.info("Successfully registered service provider for "
                        + APIConstants.ServerStartupListenerConstants.DEVPORTAL_CLIENT_APP_NAME);
            }
        } catch (APIMgtDAOException e) {
            log.error("Error while retrieving or persisting client credentials information for the portal applications",
                    e);
        } catch (APIManagementException e) {
            log.error("Error while registering the Service Provider for Portal Application", e);
        }
    }

    private static String getLoopbackOrigin() {
        int mgtTransportPort = APIUtil.getCarbonTransportPort("https");
        return "https://" + APIUtil.getHostAddress() + ":" + mgtTransportPort;
    }

    private static String generateCallbackUrl(String appContext, String serverUrl) {
        String loginCallbackUrl = serverUrl + APIConstants.ServerStartupListenerConstants.PATH_SEPARATOR + appContext
                + APIConstants.ServerStartupListenerConstants.LOGIN_CALLBACK_URL_SUFFIX;
        String logoutCallbackUrl = serverUrl + APIConstants.ServerStartupListenerConstants.PATH_SEPARATOR + appContext
                + APIConstants.ServerStartupListenerConstants.LOGOUT_CALLBACK_URL_SUFFIX;
        return "regexp=(" + loginCallbackUrl + "|" + logoutCallbackUrl + ")";
    }

    private static Map<String, String> sendDCRRequest(String clientName, String callbackUrl, String dcrUrl)
            throws APIManagementException {
        Map<String, String> result = new HashMap<>();
        try {
            String authorizationEncodedToken = APIUtil.getBase64EncodedAdminCredentials();
            JSONObject payload = new JSONObject();
            payload.put("clientName", clientName);
            payload.put("callbackUrl", callbackUrl);
            payload.put("owner", APIUtil.getAdminUsername());
            payload.put("grantType", APIConstants.ServerStartupListenerConstants.GRANT_TYPE);
            payload.put("saasApp", APIConstants.ServerStartupListenerConstants.IS_SAAS_APP);
            URL serviceEndpointURL = new URL(dcrUrl);
            HttpClient httpClient = APIUtil
                    .getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
            HttpPost httpPost = new HttpPost(dcrUrl);
            String authHeader = "Basic " + authorizationEncodedToken;
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            StringEntity requestEntity = new StringEntity(payload.toJSONString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);
            //Execute DCR request
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    || response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                String responseStr = EntityUtils.toString(entity);
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseStr);
                //Retrieve clientId and clientSecret from the DCR response
                String clientId = (String) obj.get(APIConstants.ServerStartupListenerConstants.CLIENT_ID);
                String clientSecret = (String) obj.get(APIConstants.ServerStartupListenerConstants.CLIENT_SECRET);
                result.put(APIConstants.ServerStartupListenerConstants.CLIENT_ID, clientId);
                result.put(APIConstants.ServerStartupListenerConstants.CLIENT_SECRET, clientSecret);
            } else {
                String error = "Error while starting the process:  " + response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                log.error(error);
            }
        } catch (APIMgtInternalException e) {
            String errorMsg = "Error while retrieving admin credential information";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        } catch (ClientProtocolException e) {
            String errorMsg = "Error while creating the http client for client app " + clientName;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Error while connecting to dcr endpoint for client app " + clientName;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        } catch (ParseException e) {
            String errorMsg = "Error while parsing response from DCR endpoint for client app " + clientName;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
        return result;
    }

    @Override
    public void completingServerStartup() {
    }
}

/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.*;
import org.wso2.carbon.utils.CarbonUtils;


public class OAuth2TokenValidationServiceClient {

    private static final Log log = LogFactory.getLog(OAuth2TokenValidationServiceClient.class);

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private OAuth2TokenValidationServiceStub oAuth2TokenValidationServiceStub;
    private String username;
    private String password;
    private String cookie;

    public OAuth2TokenValidationServiceClient() throws APIManagementException {
        KeyManagerConfiguration config = KeyManagerHolder.getKeyManagerInstance().getKeyManagerConfiguration();
        String serviceURL = config.getParameter(APIConstants.AUTHSERVER_URL);
        username = "admin";
        password = "admin";
        if (serviceURL == null) {
            throw new APIManagementException("Required connection details for the key management " +
                    "server not provided. Failed to create OAuth2 token validation service client");
        }

        try {
            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            oAuth2TokenValidationServiceStub = getOAuth2TokenValidationServiceStub(serviceURL, ctx);

            ServiceClient client = oAuth2TokenValidationServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setCallTransportCleanup(true);
            options.setManageSession(true);

        } catch (AxisFault axisFault) {
            log.error("Error while initializing the OAuth2 token validation service stub", axisFault);
            throw new APIManagementException("Error while initializing the OAuth2 token validation service stub",
                    axisFault);
        }
    }

    protected OAuth2TokenValidationServiceStub getOAuth2TokenValidationServiceStub(String serviceURL,
            ConfigurationContext ctx) throws AxisFault {
        return new OAuth2TokenValidationServiceStub(ctx, serviceURL +
                "OAuth2TokenValidationService");
    }

    public OAuth2ClientApplicationDTO validateAuthenticationRequest(String accessTokenIdentifier)
            throws APIManagementException {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuth2TokenValidationServiceStub._getServiceClient());
        if (cookie != null) {
            oAuth2TokenValidationServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
                    cookie);
        }

        OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
                new org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessToken.setTokenType("bearer");
        accessToken.setIdentifier(accessTokenIdentifier);
        oauthReq.setAccessToken(accessToken);

        OAuth2TokenValidationRequestDTO_TokenValidationContextParam[]
                oAuth2TokenValidationRequestDTO_tokenValidationContextParams = new
                OAuth2TokenValidationRequestDTO_TokenValidationContextParam[1];

        // Setting dummy value to skip error behaviour in service
        OAuth2TokenValidationRequestDTO_TokenValidationContextParam contextParamElement = new
                OAuth2TokenValidationRequestDTO_TokenValidationContextParam();
        contextParamElement.setKey("dummy");
        contextParamElement.setValue("dummy");

        oAuth2TokenValidationRequestDTO_tokenValidationContextParams[0] = contextParamElement;

        oauthReq.setContext(oAuth2TokenValidationRequestDTO_tokenValidationContextParams);

        OAuth2ClientApplicationDTO oAuthConsumerIfTokenIsValid;

        try {
            oAuthConsumerIfTokenIsValid = oAuth2TokenValidationServiceStub.findOAuthConsumerIfTokenIsValid(oauthReq);

        } catch (Exception e) {
            log.error("Error while validating OAuth2 request", e);
            throw new APIManagementException("Error while validating OAuth2 request", e);
        }

        ServiceContext serviceContext = oAuth2TokenValidationServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        return oAuthConsumerIfTokenIsValid;
    }

}
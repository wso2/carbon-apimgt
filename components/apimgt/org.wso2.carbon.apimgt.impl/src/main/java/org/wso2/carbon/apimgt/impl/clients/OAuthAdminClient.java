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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.utils.CarbonUtils;


public class OAuthAdminClient {

    private static final Log log = LogFactory.getLog(OAuthAdminClient.class);

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private OAuthAdminServiceStub oAuthAdminServiceStub;
    private String username;
    private String password;
    private String cookie;

    public OAuthAdminClient() throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        username = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
        password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
        if (serviceURL == null || username == null || password == null) {
            throw new APIManagementException("Required connection details for the key management server not provided");
        }

        try {
            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            oAuthAdminServiceStub = new OAuthAdminServiceStub(ctx, serviceURL + "OAuthAdminService");

            ServiceClient client = oAuthAdminServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setCallTransportCleanup(true);
            options.setManageSession(true);

        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while initializing the OAuth admin service stub", axisFault);
        }
    }

    public OAuthConsumerAppDTO[] getAllOAuthApplicationData() throws Exception {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        OAuthConsumerAppDTO[] oAuthConsumerAppDTOs = oAuthAdminServiceStub.getAllOAuthApplicationData();

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        return oAuthConsumerAppDTOs;
    }

    public OAuthConsumerAppDTO getOAuthApplicationData(String consumerkey) throws Exception {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        OAuthConsumerAppDTO oAuthConsumerAppDTO = oAuthAdminServiceStub.getOAuthApplicationData(consumerkey);

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        return oAuthConsumerAppDTO;

    }

    public OAuthConsumerAppDTO getOAuthApplicationDataByAppName(String appName) throws Exception {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        OAuthConsumerAppDTO oAuthConsumerAppDTO = oAuthAdminServiceStub.getOAuthApplicationDataByAppName(appName);

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

        return oAuthConsumerAppDTO;
    }


    // TODO : this method should return app data
    public void registerOAuthApplicationData(OAuthConsumerAppDTO application) throws Exception {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        oAuthAdminServiceStub.registerOAuthApplicationData(application);

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
    }

    // TODO : this method should be removed once above is done
    public OAuthConsumerAppDTO getOAuthApplicationDataByName(String applicationName) throws Exception {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        OAuthConsumerAppDTO[] dtos = oAuthAdminServiceStub.getAllOAuthApplicationData();

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

        if (dtos != null && dtos.length > 0) {
            for (OAuthConsumerAppDTO dto : dtos) {
                if (applicationName.equals(dto.getApplicationName())) {
                    return dto;
                }
            }
        }
        return null;

    }

    public void removeOAuthApplicationData(String consumerkey) throws Exception {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        oAuthAdminServiceStub.removeOAuthApplicationData(consumerkey);

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
    }

    public void updateOAuthApplicationData(OAuthConsumerAppDTO consumerAppDTO) throws Exception {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        oAuthAdminServiceStub.updateConsumerApplication(consumerAppDTO);

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

    }

    public OAuthConsumerAppDTO[] getAppsAuthorizedByUser() throws Exception {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        OAuthConsumerAppDTO[] oAuthConsumerAppDTOs = oAuthAdminServiceStub.getAppsAuthorizedByUser();

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        return oAuthConsumerAppDTOs;

    }

    public OAuthRevocationResponseDTO revokeAuthzForAppsByRessourceOwner(OAuthRevocationRequestDTO
                                                                                 reqDTO) throws Exception {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.
                    COOKIE_STRING, cookie);
        }

        OAuthRevocationResponseDTO oAuthRevocationResponseDTO = oAuthAdminServiceStub.
                revokeAuthzForAppsByResoureOwner(reqDTO);

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

        return oAuthRevocationResponseDTO;

    }

    public String[] getAllowedOAuthGrantTypes() throws Exception {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, oAuthAdminServiceStub._getServiceClient());
        if (cookie != null) {
            oAuthAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        String[] allowedGrantTypes = null;

        if (allowedGrantTypes == null) {
            allowedGrantTypes = oAuthAdminServiceStub.getAllowedGrantTypes();
        }

        ServiceContext serviceContext = oAuthAdminServiceStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

        return allowedGrantTypes;
    }
}
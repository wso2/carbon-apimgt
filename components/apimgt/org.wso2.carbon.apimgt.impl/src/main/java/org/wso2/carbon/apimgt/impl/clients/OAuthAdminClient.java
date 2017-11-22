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
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

@SuppressWarnings("unused")
public class OAuthAdminClient {

    private static final Log log = LogFactory.getLog(OAuthAdminClient.class);

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private OAuthAdminServiceStub oAuthAdminServiceStub;
    private String cookie;

    public OAuthAdminClient() throws APIManagementException {
        KeyManagerConfiguration configuration = KeyManagerHolder.getKeyManagerInstance().getKeyManagerConfiguration();
        String serviceURL = configuration.getParameter(APIConstants.AUTHSERVER_URL);
        if (serviceURL == null) {
            throw new APIManagementException("Required connection details for the key management server not provided");
        }

        try {
            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            oAuthAdminServiceStub = getOAuthAdminServiceStub(serviceURL, ctx);

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

    protected OAuthAdminServiceStub getOAuthAdminServiceStub(String serviceURL, ConfigurationContext ctx)
            throws AxisFault {
        return new OAuthAdminServiceStub(ctx, serviceURL + "OAuthAdminService");
    }

    public OAuthConsumerAppDTO getOAuthApplicationData(String consumerKey, String username) throws Exception {
        Util.setAuthHeaders(oAuthAdminServiceStub._getServiceClient(), username);
        return oAuthAdminServiceStub.getOAuthApplicationData(consumerKey);

    }

    public OAuthConsumerAppDTO getOAuthApplicationDataByAppName(String appName, String username) throws Exception {
        Util.setAuthHeaders(oAuthAdminServiceStub._getServiceClient(), username);
        return oAuthAdminServiceStub.getOAuthApplicationDataByAppName(appName);
    }

    public void registerOAuthApplicationData(OAuthConsumerAppDTO application, String username) throws Exception {
        Util.setAuthHeaders(oAuthAdminServiceStub._getServiceClient(), username);
        oAuthAdminServiceStub.registerOAuthApplicationData(application);

    }

    public void removeOAuthApplicationData(String consumerKey, String username) throws Exception {
        Util.setAuthHeaders(oAuthAdminServiceStub._getServiceClient(), username);
        oAuthAdminServiceStub.removeOAuthApplicationData(consumerKey);
    }

    public void updateOAuthApplicationData(OAuthConsumerAppDTO consumerAppDTO, String username) throws Exception {
        Util.setAuthHeaders(oAuthAdminServiceStub._getServiceClient(), username);
        oAuthAdminServiceStub.updateConsumerApplication(consumerAppDTO);
    }

}
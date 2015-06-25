/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.handlers.security.stub.APIAuthenticationServiceStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;

import java.net.URL;

public class APIAuthenticationServiceClient {

    private static Log log = LogFactory.getLog(APIAuthenticationServiceClient.class);

    private APIAuthenticationServiceStub stub;

    public APIAuthenticationServiceClient(String backendServerURL, String username, String password)
            throws Exception {
        try {
            AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(null, backendServerURL + "AuthenticationAdmin");
            ServiceClient authAdminServiceClient = authenticationAdminStub._getServiceClient();
            authAdminServiceClient.getOptions().setManageSession(true);
            authenticationAdminStub.login(username, password, new URL(backendServerURL).getHost());
            ServiceContext serviceContext = authenticationAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String authenticatedCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            if (log.isDebugEnabled()) {
                log.debug("Authentication Successful with AuthenticationAdmin. " +
                          "Authenticated Cookie ID : " + authenticatedCookie);
            }

            stub = new APIAuthenticationServiceStub(
                    null, backendServerURL + "APIAuthenticationService");
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                authenticatedCookie);
        } catch (Exception e) {
            String errorMsg = "Error when instantiating APIAuthenticationServiceClient.";
            log.error(errorMsg, e);
            throw e;
        }
    }

    public void invalidateKey(String accessToken) throws AxisFault {
        try {
            stub.invalidateKey(accessToken);
        } catch (Exception e) {
            throw new AxisFault("Error while invalidating API keys", e);
        }
    }

}

/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.stub.provider.APIKeyMgtProviderServiceStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * This class will be used to access APIKeyMgtProviderService hosted in key manager
 */
public class ProviderKeyMgtClient {

    private static Log log = LogFactory.getLog(ProviderKeyMgtClient.class);

    private APIKeyMgtProviderServiceStub providerServiceStub;

    public ProviderKeyMgtClient(String backendServerURL, String username, String password)
            throws APIManagementException {
        try {
            AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(null, backendServerURL +
                                                                                                "AuthenticationAdmin");
            ServiceClient authAdminServiceClient = authenticationAdminStub._getServiceClient();
            authAdminServiceClient.getOptions().setManageSession(true);
            authenticationAdminStub.login(username, password, new URL(backendServerURL).getHost());
            ServiceContext serviceContext = authenticationAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String authenticatedCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            if (log.isDebugEnabled()) {
                log.debug("Authentication Successful with AuthenticationAdmin. Authenticated Cookie ID : " +
                          authenticatedCookie);
            }

            providerServiceStub = new APIKeyMgtProviderServiceStub(null, backendServerURL + "APIKeyMgtProviderService");
            ServiceClient client = providerServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, authenticatedCookie);
        } catch (RemoteException e) {
            handleException("Cannot connect to the service, instantiating ProviderKeyMgtClient failed", e);
        } catch (LoginAuthenticationExceptionException e) {
            handleException("Error during Authentication, instantiating ProviderKeyMgtClient failed", e);
        } catch (MalformedURLException e) {
            handleException("Invalid backendServerURL, instantiating ProviderKeyMgtClient failed", e);
        }
    }

    /**
     * Removes passed consumer keys from scope cache
     *
     * @param consumerKeys
     * @throws Exception
     */
    public void removeScopeCache(String[] consumerKeys) throws APIManagementException {
        try {
            providerServiceStub.removeScopeCache(consumerKeys);
        } catch (RemoteException e) {
            handleException("RemoteException occured while removing scope cache ", e);
        }
    }

    /**
     * logs the error message and throws an exception
     *
     * @param message   Error message
     * @param throwable
     * @throws APIManagementException
     */
    private static void handleException(String message, Throwable throwable) throws APIManagementException {
        log.error(message, throwable);
        throw new APIManagementException(throwable);
    }

}
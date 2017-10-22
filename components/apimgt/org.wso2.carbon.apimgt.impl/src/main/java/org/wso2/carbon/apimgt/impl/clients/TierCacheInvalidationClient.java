/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.clients;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

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
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tier.cache.stub.TierCacheServiceStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

public class TierCacheInvalidationClient {
    private static final Log log = LogFactory.getLog(TierCacheInvalidationClient.class);
    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;
    
    String storeServerURL;
    
    String storeUserName;
    
    String storePassword;

    public TierCacheInvalidationClient() throws APIManagementException {
        APIManagerConfiguration config =
                                         ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                                               .getAPIManagerConfiguration();
        storeServerURL = config.getFirstProperty(APIConstants.API_STORE_SERVER_URL);
        storeUserName = config.getFirstProperty(APIConstants.API_STORE_USERNAME);
        storePassword = config.getFirstProperty(APIConstants.API_STORE_PASSWORD);
    }

    public void clearCaches(String tenantDomain) {
        String cookie;

        // Clear Store Cache
        try {
            cookie = login(storeServerURL, storeUserName, storePassword);
            clearCache(tenantDomain, storeServerURL, cookie);
        } catch (AxisFault axisFault) {
            log.error("Error while initializing the OAuth admin service stub in Store for tenant : " + tenantDomain,
                      axisFault);
        } catch (RemoteException e) {
            log.error("Error while invalidating the tier cache in Store for tenant : " + tenantDomain, e);
        }
        
    }

    public void clearCache(String tenantDomain, String serverURL, String cookie) throws AxisFault, RemoteException {
        TierCacheServiceStub tierCacheServiceStub;

        ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        tierCacheServiceStub = getTierCacheServiceStub(serverURL, ctx);
        ServiceClient client = tierCacheServiceStub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
        options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);

        tierCacheServiceStub.invalidateCache(tenantDomain);
    }

    private String login(String serverURL, String userName, String password) throws AxisFault {
        if (serverURL == null || userName == null || password == null) {
            throw new AxisFault("Required admin configuration unspecified");
        }

        String host;
        try {
            host = new URL(serverURL).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("Server URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = getAuthenticationAdminStub(serverURL);
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(userName, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating ", e);
        }
    }

    protected AuthenticationAdminStub getAuthenticationAdminStub(String serverURL) throws AxisFault {
        return new AuthenticationAdminStub(null, serverURL + "AuthenticationAdmin");
    }

    protected TierCacheServiceStub getTierCacheServiceStub(String serverURL, ConfigurationContext ctx)
            throws AxisFault {
        return new TierCacheServiceStub(ctx, serverURL + "TierCacheService");
    }

}

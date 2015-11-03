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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
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
import org.wso2.carbon.context.CarbonContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TierCacheInvalidationClient {
    private static final Log log = LogFactory.getLog(TierCacheInvalidationClient.class);
    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    Map<String, Environment> environments;

    public TierCacheInvalidationClient() throws APIManagementException {
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        environments = config.getApiGatewayEnvironments();
    }

    public void clearCaches(String tenantDomain){
        String gatewayServerURL;

        if(environments == null || environments.isEmpty()){
            if(log.isDebugEnabled()){
                log.debug("Unable to find the list of gateway environments");
            }
            return;
        }

        for (Map.Entry<String, Environment> entry : environments.entrySet()) {
            gatewayServerURL = entry.getValue().getServerURL();

            TierCacheServiceStub tierCacheServiceStub;
            try {
                String cookie = login(entry.getValue());
                ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
                tierCacheServiceStub = new TierCacheServiceStub(ctx, gatewayServerURL + "TierCacheService");
                ServiceClient client = tierCacheServiceStub._getServiceClient();
                Options options = client.getOptions();
                options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setManageSession(true);
                options.setProperty(HTTPConstants.COOKIE_STRING, cookie);

            } catch (AxisFault axisFault) {
               log.error("Error while initializing the OAuth admin service stub for gateway environment : " +
                         entry.getValue().getName() + " for tenant : " + tenantDomain, axisFault);
                continue;
            }
            try {
                tierCacheServiceStub.invalidateCache(tenantDomain);
            } catch (RemoteException e) {
                log.error("Error while invalidating the tier cache for gateway environment : " +
                          entry.getValue().getName() + " for tenant : " + tenantDomain, e);
            }
        }
    }

    private String login(Environment environment) throws AxisFault {
        String user = environment.getUserName();
        String password = environment.getPassword();
        String serverURL = environment.getServerURL();

        if (serverURL == null || user == null || password == null) {
            throw new AxisFault("Required API gateway admin configuration unspecified");
        }

        String host;
        try {
            host = new URL(serverURL).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("API gateway URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, serverURL + "AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(user, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the API gateway admin", e);
        }
    }

}

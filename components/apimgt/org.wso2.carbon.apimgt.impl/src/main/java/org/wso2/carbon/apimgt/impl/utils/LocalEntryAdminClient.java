/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.URL;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.localentry.stub.APILocalEntryAdminStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

/**
 * A Service Implementation for the LocalEntryAdmin service. This class is implemented as a wrapper for
 * the LocalEntryAdminServiceStub. This implementation loads the necessary service
 * endpoint information and admin user credentials required to invoke the admin service
 * from the APIManagerConfiguration.
 */
public class LocalEntryAdminClient {
    private APILocalEntryAdminStub localEntryAdminServiceStub;
    private String tenantDomain;

    public LocalEntryAdminClient(Environment environment, String tenantDomain) throws AxisFault {
        this.tenantDomain = tenantDomain;
        localEntryAdminServiceStub = new APILocalEntryAdminStub(null,
                environment.getServerURL() + "APILocalEntryAdmin");
        setup(localEntryAdminServiceStub, environment);
        CarbonUtils.setBasicAccessSecurityHeaders(environment.getUserName(), environment.getPassword(),
                localEntryAdminServiceStub._getServiceClient());
    }

    protected final void setup(Stub stub, Environment environment) throws AxisFault {
        String cookie = gatewayLogin(environment);
        ServiceClient serviceClient = stub._getServiceClient();
        Options options = serviceClient.getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);

    }

    private String gatewayLogin(Environment environment) throws AxisFault {
        String userName = environment.getUserName();
        String password = environment.getPassword();
        String serverURL = environment.getServerURL();

        if (serverURL == null || userName == null || password == null) {
            throw new AxisFault("Required API gateway admin configuration unspecified");
        }
        String host;
        host = new URL(serverURL).getHost();
        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null,
                serverURL + "AuthenticationAdmin");
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
            throw new AxisFault("Error while authenticating against the API gateway admin", e);
        }
    }

    /**
     * This method is used to add the Local Entry.
     *
     * @param content Content of the Local Entry.
     * @throws AxisFault If error occurs when adding Local Entry.
     */
    public void addLocalEntry(String content) throws AxisFault {
        try {
            localEntryAdminServiceStub.addLocalEntry(content, tenantDomain);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while adding the Local Entry: ", e.getMessage(), e);
        }
    }

    /**
     * This method is used to get the LocalEntry.
     *
     * @param key Local entry key to be retrieved.
     * @return LocalEntry
     * @throws AxisFault If error occurs when retrieving Local Entry.
     */
    public Boolean localEntryExists(String key) throws AxisFault {
        try {
            Object localEntryObject = localEntryAdminServiceStub.getEntry(key, tenantDomain);
            return localEntryObject != null;
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while getting the Local Entry: ", e.getMessage(), e);
        }
    }

    /**
     * This method is used to delete given Local Entry.
     *
     * @param key Local Entry key to be deleted.
     * @return Status of the operation.
     * @throws AxisFault If error occurs when deleting the Local Entry.
     */
    public boolean deleteEntry(String key) throws AxisFault {
        try {
            return localEntryAdminServiceStub.deleteLocalEntry(key, tenantDomain);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while deleting the Local Entry: ", e.getMessage(), e);
        }
    }
}
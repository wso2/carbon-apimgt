/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.um.ws.api.stub.RemoteAuthorizationManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Service client implementation for the RemoteAuthorizationManagerService (an admin service
 * offered by the remote-user-manager component). This class is implemented as a wrapper for
 * the RemoteAuthorizationManagerServiceStub. This implementation loads the necessary service
 * endpoint information and admin user credentials required to invoke the admin service
 * from the APIManagerConfiguration. All invocations of the RemoteAuthorizationManagerService
 * are properly secured with UsernameToken security. This implementation is not thread safe
 * and hence must not be shared among multiple threads at the same time.
 */
class RemoteAuthorizationManagerClient {

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private RemoteAuthorizationManagerServiceStub authorizationManager;
    private RemoteUserStoreManagerServiceStub userStoreManager;
    private String username;
    private String password;
    private String cookie;

    public RemoteAuthorizationManagerClient() throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        username = config.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME);
        password = config.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD);
        if (serviceURL == null || username == null || password == null) {
            throw new APIManagementException("Required connection details for authentication " +
                    "manager not provided");
        }

        try {
            authorizationManager = new RemoteAuthorizationManagerServiceStub(null, serviceURL +
                    "RemoteAuthorizationManagerService");
            userStoreManager = new RemoteUserStoreManagerServiceStub(null, serviceURL +
                    "RemoteUserStoreManagerService");
            for (ServiceClient client : new ServiceClient[] {
                    authorizationManager._getServiceClient(),
                    userStoreManager._getServiceClient()}) {
                Options options = client.getOptions();
                options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setCallTransportCleanup(true);
                options.setManageSession(true);
            }
        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while initializing the user management stubs",
                    axisFault);
        }
    }

    /**
     * Query the remote user manager to find out whether the specified user has the
     * specified permission.
     *
     * @param user Username
     * @param permission A valid Carbon permission
     * @return true if the user has the specified permission and false otherwise
     * @throws APIManagementException If and error occurs while accessing the admin service
     */
    public boolean isUserAuthorized(String user, String permission) throws APIManagementException {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, authorizationManager._getServiceClient());
        if (cookie != null) {
            authorizationManager._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        try {
            boolean authorized = authorizationManager.isUserAuthorized(user, permission,
                    CarbonConstants.UI_PERMISSION_ACTION);
            ServiceContext serviceContext = authorizationManager.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return authorized;
        } catch (Exception e) {
            throw new APIManagementException("Error while accessing backend services for " +
                    "user permission validation", e);
        }
    }

    /**
     * Query the remote user manager and retrieve the list of role names associated for the given
     * user name. This is used when authorization for certain actions are appropriately delegated
     * to other components (ex:- Lifecycle Management).
     *
     * @param user Username
     * @return the list of roles to which the user belongs to.
     * @throws APIManagementException If and error occurs while accessing the admin service
     */
    public String[] getRolesOfUser(String user) throws APIManagementException {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, userStoreManager._getServiceClient());
        if (cookie != null) {
            userStoreManager._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        try {
            String[] roles = userStoreManager.getRoleListOfUser(user);
            ServiceContext serviceContext = userStoreManager.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return roles;
        } catch (Exception e) {
            throw new APIManagementException("Error while accessing backend services for " +
                    "user role list", e);
        }
    }


    /**
     * Query the remote user manager and retrieve the list of role names in users-store
     *
     *

     * @return the list of roles
     * @throws APIManagementException If and error occurs while accessing the admin service
     */
    public String[] getRoleNames() throws APIManagementException {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                                                  true, userStoreManager._getServiceClient());
        if (cookie != null) {
            userStoreManager._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }

        try {
            String[] roles = userStoreManager.getRoleNames();
            ServiceContext serviceContext = userStoreManager.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return roles;
        } catch (Exception e) {
            throw new APIManagementException("Error while accessing backend services for " +
                                             "getting list of all the roles.", e);
        }
    }
}

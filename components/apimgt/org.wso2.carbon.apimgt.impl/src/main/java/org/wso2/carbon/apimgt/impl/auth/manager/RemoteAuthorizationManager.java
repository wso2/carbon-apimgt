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

package org.wso2.carbon.apimgt.impl.auth.manager;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class RemoteAuthorizationManager {

    private AuthorizationManagerClientFactory clientFactory;
    private static RemoteAuthorizationManager instance;
    private String type;

    private ScheduledExecutorService exec;
    private ScheduledFuture future;

    private RemoteAuthorizationManager(String type) {
        AuthorizationManagerClientFactory.ClientType authClientType =
                ("".equals(type) || type == null) ?
                        AuthorizationManagerClientFactory.ClientType.STANDALONE :
                        AuthorizationManagerClientFactory.ClientType.valueOf(type);
        this.clientFactory = AuthorizationManagerClientFactory.getAuthorizationManagerClientFactory(authClientType);
    }

    public static RemoteAuthorizationManager getInstance() {
        String type =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty("type");
        if (instance == null) {
            synchronized (RemoteAuthorizationManager.class) {
                if (instance == null) {
                    instance = new RemoteAuthorizationManager(type);
                }
            }
        }
        return instance;
    }

    public boolean isUserAuthorized(String user, String permission) throws APIManagementException {
        AuthorizationManagerClient client = null;
        try {
            client = clientFactory.getAuthorizationManagerClient();
            return client.isUserAuthorized(user, permission);

        } catch (Exception e) {
            throw new APIManagementException("Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientFactory.releaseAuthorizationManagerClient(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public String[] getRolesOfUser(String user) throws APIManagementException {
        AuthorizationManagerClient client = null;
        try {
            client = clientFactory.getAuthorizationManagerClient();
            return client.getRolesOfUser(user);

        } catch (Exception e) {
            throw new APIManagementException("Error while retrieving role list of user", e);
        } finally {
            try {
                if (client != null) {
                    clientFactory.releaseAuthorizationManagerClient(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public String[] getRoleNames() throws APIManagementException {
        AuthorizationManagerClient client = null;
        try {
            client = clientFactory.getAuthorizationManagerClient();
            return client.getRoleNames();

        } catch (Exception e) {
            throw new APIManagementException("Error while retrieving the roles list of the system.", e);
        } finally {
            try {
                if (client != null) {
                    clientFactory.releaseAuthorizationManagerClient(client);
                }
            } catch (Exception ignored) {
            }
        }
    }


}

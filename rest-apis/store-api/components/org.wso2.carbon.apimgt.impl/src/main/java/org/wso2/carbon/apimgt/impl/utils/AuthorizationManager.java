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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;



public class AuthorizationManager {

    private static volatile AuthorizationManager instance;

    private ObjectPool clientPool;
   

    private static final Log log = LogFactory.getLog(AuthorizationManager.class);

    public enum ClientType {
        REMOTE, STANDALONE
    }

    private AuthorizationManager(ClientType authClientType) {
        init(authClientType);
    }

    public static AuthorizationManager getInstance() {
        if (instance == null) {
            synchronized (AuthorizationManager.class) {
                if (instance == null) {
                    String strChekPermRemotely = ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService().getAPIManagerConfiguration().
                                    getFirstProperty(APIConstants.ConfigParameters.CHECK_PERMISSIONS_REMOTELY);

                    if (strChekPermRemotely == null || "".equals(strChekPermRemotely)) {
                        strChekPermRemotely = "false";

                        if (log.isDebugEnabled()) {
                            log.debug("CheckPermissionsRemotely attribute is not configured in Authorization Manager " +
                                    "configuration, Therefore assuming that the internal Authorization Manager " +
                                    "Client implementation is being used");
                        }
                    }

                    boolean checkPermRemotely = Boolean.parseBoolean(strChekPermRemotely);

                    if (log.isDebugEnabled()) {
                        log.debug("IsExternal attribute is set to '" + checkPermRemotely + '\'');
                        if (checkPermRemotely) {
                            log.debug("Remote Authorization Manager Client implementation will be used");
                        } else {
                            log.debug("Standalone Authorization Manager Client implementation will be used");
                        }
                    }
                    ClientType authClientType = (!checkPermRemotely) ? ClientType.STANDALONE : ClientType.REMOTE;
                    instance = new AuthorizationManager(authClientType);
                }
            }
        }
        return instance;
    }

    private void init(final ClientType type) {
        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return AuthorizationManagerClientFactory.getAuthorizationManagerClient(type);
            }
        });
    }

    public void destroy() {
        try {
            clientPool.close();
        } catch (Exception ignored) {
        }

    }

    public boolean isUserAuthorized(String user, String permission) throws APIManagementException {
        AuthorizationManagerClient client = null;
        try {
            client = (AuthorizationManagerClient) clientPool.borrowObject();
            return client.isUserAuthorized(user, permission);

        } catch (Exception e) {
            throw new APIManagementException("Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.returnObject(client);
                }
            } catch (Exception ignored) {
                //Ignore
            }
        }
    }

    public String[] getRolesOfUser(String user) throws APIManagementException {
        AuthorizationManagerClient client = null;
        try {
            client = (AuthorizationManagerClient) clientPool.borrowObject();
            return client.getRolesOfUser(user);

        } catch (Exception e) {
            throw new APIManagementException("Error while retrieving role list of user", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.returnObject(client);
                }
            } catch (Exception ignored) {
                //Ignore
            }
        }
    }

    public String[] getRoleNames() throws APIManagementException {
        AuthorizationManagerClient client = null;
        try {
            client = (AuthorizationManagerClient) clientPool.borrowObject();
            return client.getRoleNames();

        } catch (Exception e) {
            throw new APIManagementException("Error while retrieving the roles list of the system.", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.returnObject(client);
                }
            } catch (Exception ignored) {
                //Ignore
            }
        }
    }


}

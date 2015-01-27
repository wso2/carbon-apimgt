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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class AuthorizationManager {

    private static AuthorizationManager instance;

    private ObjectPool clientPool;

    private ScheduledExecutorService exec;
    private ScheduledFuture future;

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
                    String strIsExternal = ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService().getAPIManagerConfiguration().
                                    getFirstProperty(APIConstants.ConfigParameters.IS_EXTERNAL);
                    if (strIsExternal == null || "".equals(strIsExternal)) {
                        if (log.isDebugEnabled()) {
                            log.debug("IsExternal attribute is not configured in Authorization Manager " +
                                    "configuration, Therefore assuming that the internal Authorization Manager " +
                                    "Client implementation is being used");
                        }
                    }
                    boolean isExternal = Boolean.parseBoolean(ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService().getAPIManagerConfiguration().
                                    getFirstProperty(APIConstants.ConfigParameters.IS_EXTERNAL));
                    if (log.isDebugEnabled()) {
                        log.debug("IsExternal attribute is set to '" + isExternal + "'");
                        if (isExternal) {
                            log.debug("Remote Authorization Manager Client implementation will be used");
                        } else {
                            log.debug("Standalone Authorization Manager Client implementation will be used");
                        }
                    }
                    ClientType authClientType = (!isExternal) ? ClientType.STANDALONE : ClientType.REMOTE;
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
            }
        }
    }


}

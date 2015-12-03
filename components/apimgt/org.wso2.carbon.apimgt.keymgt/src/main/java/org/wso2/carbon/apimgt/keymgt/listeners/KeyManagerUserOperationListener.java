/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.keymgt.listeners;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;

import java.util.Map;
import java.util.Set;

public class KeyManagerUserOperationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(KeyManagerUserOperationListener.class);

    /**
     * Bundle execution order id.
     */
    @Override
    public int getExecutionOrderId() {
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 80;
    }

    /**
     * Deleting user from the identity database prerequisites.
     */
    @Override
    public boolean doPreDeleteUser(java.lang.String username,
                                   org.wso2.carbon.user.core.UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        return !isEnable() || removeGatewayKeyCache(username);
    }

    private boolean removeGatewayKeyCache(String username){

        APIManagerConfiguration config = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (config.getApiGatewayEnvironments().size() <= 0) {
            return true;
        }

        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        Set<String> activeTokens;

        try {
            activeTokens = apiMgtDAO.getActiveAccessTokensOfUser(username);
        } catch (APIManagementException e) {
            log.error("Error while getting active access tokens of user " + username, e);
            return false;
        }

        if (activeTokens == null || activeTokens.isEmpty()) {
            if(log.isDebugEnabled()){
                log.debug("No active tokens found for the user " + username);
            }
            return true;
        }

        if(log.isDebugEnabled()){
            log.debug("Found " + activeTokens.size() + " active tokens of the user " + username);
        }

        Map<String, Environment> gatewayEnvs = config.getApiGatewayEnvironments();

        for (Environment environment : gatewayEnvs.values()) {
            if(log.isDebugEnabled()){
                log.debug("Going to remove tokens from the cache of the Gateway '" + environment.getName() + "'");
            }
            try {
                APIAuthenticationAdminClient client = new APIAuthenticationAdminClient(environment);
                client.invalidateCachedTokens(activeTokens);

                log.debug("Removed cached tokens of the Gateway.");
            } catch (AxisFault axisFault) {
                //log and ignore since we do not have to halt the user operation due to cache invalidation failures.
                log.error("Error occurred while invalidating the Gateway Token Cache of Gateway '" +
                                                                            environment.getName() + "'", axisFault);
            }
        }

        return true;
    }
}

/*
 *
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  n compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This interceptor checks user permission
 */
public class PermissionValidationInterceptor extends AbstractPhaseInterceptor {

    private List<String> permissions = Collections.emptyList();
    private List<String> excludePathsList;
    private AuthorizationPolicy authorizationPolicy;
    private static final Log log = LogFactory.getLog(PermissionValidationInterceptor.class);
    private static final String EXCLUDE_PATHS_SYS_PROP = "exclude.paths";

    public PermissionValidationInterceptor() {
        // We will use PRE_INVOKE phase as we need to process message before hit actual
        // service
        super(Phase.PRE_INVOKE);
        String excludePaths = System.getProperty(EXCLUDE_PATHS_SYS_PROP);
        if (excludePaths != null) {
            String[] exludePathsArr = excludePaths.split(",");
            excludePathsList = new ArrayList<>(Arrays.asList(exludePathsArr));
        }
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy != null) {
            String username = policy.getUserName();
            if (excludePathsList != null) {
                String path = (String) message.get(Message.PATH_INFO);
                if (excludePathsList.contains(path)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Path " + path + " is excluded");
                    }
                    return;
                }
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to validate permission " + permissions + " for user: " + username);
                }
                boolean hasPermission = false;
                for (String permission : permissions) {
                    if (APIUtil.hasPermission(username, permission)) {
                        log.debug("Permission is granted");
                        hasPermission = true;
                        break;
                    }
                }
                if (!hasPermission) {
                    throw new AuthenticationException("Unauthenticated request");
                }
            } catch (APIManagementException e) {
                log.error("Error while checking the user permission", e);
                throw new AuthenticationException("Unauthenticated request");
            }
        }
    }

    public List<String> getPermissions() {

        return permissions;
    }

    public void setPermissions(List<String> permissions) {

        this.permissions = permissions;
    }
}


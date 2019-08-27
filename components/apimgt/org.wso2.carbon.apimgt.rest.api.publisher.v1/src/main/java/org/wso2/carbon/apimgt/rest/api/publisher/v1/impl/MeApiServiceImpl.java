/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import java.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.MeApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for operations related to the logged-in user (/me endpoint)
 */
public class MeApiServiceImpl implements MeApiService {

    private static final Log log = LogFactory.getLog(MeApiServiceImpl.class);

    /**
     * Check whether the logged-in user has given role
     *
     * @param roleId Base64 URL encoded form of role name -Base64URLEncode{user-store-name/role-name}
     * @return 200 if logged-in user has given role
     */
    public Response meRolesRoleIdHead(String roleId, MessageContext messageContext) {

        String userName = RestApiUtil.getLoggedInUsername();
        boolean isUserInRole = false;

        if (roleId != null) {
            try {
                String roleName = new String(Base64.getUrlDecoder().decode(roleId));
                log.debug("Checking whether user :" + userName + " has role : " + roleName);
                isUserInRole = APIUtil.checkIfUserInRole(userName, roleName);
            } catch (UserStoreException e) {
                RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
            }
        }
        if (isUserInRole) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}

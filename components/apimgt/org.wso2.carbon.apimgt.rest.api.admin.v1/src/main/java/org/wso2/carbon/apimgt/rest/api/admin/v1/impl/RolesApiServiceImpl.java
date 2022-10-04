/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.admin.v1.RolesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.RolesCommonImpl;

import javax.ws.rs.core.Response;

public class RolesApiServiceImpl implements RolesApiService {

    /**
     * Check whether the given role exists in the system
     *
     * @param roleId Base64 URL encoded form of role name - Base64URLEncode{user-store-name/role-name}
     * @return HTTP Status Code 200 if the given role exists
     */
    @Override
    public Response validateSystemRole(String roleId, MessageContext messageContext) {
        boolean isRoleExist = RolesCommonImpl.validateSystemRole(roleId);
        if (isRoleExist) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}

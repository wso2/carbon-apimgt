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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.RolesApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.RolesApiCommonImpl;

import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for operations related to roles
 */
public class RolesApiServiceImpl implements RolesApiService {

    /**
     * Check whether the given role exists in the system
     *
     * @param roleId Base64 URL encoded form of role name -Base64URLEncode{user-store-name/role-name}
     * @return 200 if the given role exists
     */
    @Override
    public Response validateSystemRole(String roleId, MessageContext messageContext) throws APIManagementException {

        RolesApiCommonImpl.validateSystemRole(roleId);
        return Response.status(Response.Status.OK).build();
    }
}

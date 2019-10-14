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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ScopesApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Base64;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for scopes related operations
 */
public class ScopesApiServiceImpl implements ScopesApiService {
    private static final Log log = LogFactory.getLog(ScopesApiServiceImpl.class);

    /**
     * Check whether the given scope already used in APIs
     *
     * @param name           Base64 URL encoded form of scope name -Base64URLEncode{scope name}
     * @param messageContext
     * @return boolean to indicate existence
     */
    public Response validateScope(String name, MessageContext messageContext) {
        boolean isScopeExist = false;
        String scopeName = new String(Base64.getUrlDecoder().decode(name));
        if (!APIUtil.isWhiteListedScope(scopeName)) {
            try {
                APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
                String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
                isScopeExist = apiProvider.isScopeKeyExist(scopeName, tenantId);
            } catch (APIManagementException e) {
                RestApiUtil.handleInternalServerError("Error occurred while checking scope name", e, log);
            }
        }

        if (isScopeExist) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}

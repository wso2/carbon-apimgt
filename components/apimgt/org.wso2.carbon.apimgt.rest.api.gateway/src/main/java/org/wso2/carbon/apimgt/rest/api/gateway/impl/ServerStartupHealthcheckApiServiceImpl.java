/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.rest.api.gateway.ServerStartupHealthcheckApiService;

import javax.ws.rs.core.Response;


public class ServerStartupHealthcheckApiServiceImpl implements ServerStartupHealthcheckApiService {

    private static final Log log = LogFactory.getLog(ServerStartupHealthcheckApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    public Response serverStartupHealthcheckGet(MessageContext messageContext) {
        if (debugEnabled) {
            log.debug("Performing server startup health check");
        }
        boolean isAllApisDeployed = GatewayUtils.isAllApisDeployed();
        boolean isAllGatewayPoliciesDeployed = GatewayUtils.isAllGatewayPoliciesDeployed();
        if (GatewayUtils.isTenantsProvisioned() && isAllApisDeployed && isAllGatewayPoliciesDeployed) {
            log.info("Server startup health check passed - all components ready");
            return Response.status(Response.Status.OK).build();
        }
        log.warn("Server startup health check failed - tenants: " + GatewayUtils.isTenantsProvisioned() + 
                ", APIs deployed: " + isAllApisDeployed + ", policies deployed: " + 
                isAllGatewayPoliciesDeployed);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}

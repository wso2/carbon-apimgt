/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.gateway.EndPointsApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.EndpointsDTO;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

import java.util.List;

public class EndPointsApiServiceImpl implements EndPointsApiService {

    private static final Log log = LogFactory.getLog(EndPointsApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    public Response endPointsGet(String apiName, String version, String tenantDomain, MessageContext messageContext) {
        tenantDomain = RestApiCommonUtil.getValidateTenantDomain(tenantDomain);

        try {

            List<String> deployedLocalEntries = GatewayUtils.retrieveDeployedEndpoints(apiName, version,
                    tenantDomain);
            if (debugEnabled) {
                log.debug("Retrieved Artifacts for " + apiName + " from eventhub");
            }
            EndpointsDTO endpointsDTO = new EndpointsDTO();
            endpointsDTO.endpoints(deployedLocalEntries);
            return Response.ok().entity(endpointsDTO).build();
        } catch (AxisFault e) {
            String errorMessage = "Error in fetching deployed artifacts from Synapse Configuration";
            log.error(errorMessage, e);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}

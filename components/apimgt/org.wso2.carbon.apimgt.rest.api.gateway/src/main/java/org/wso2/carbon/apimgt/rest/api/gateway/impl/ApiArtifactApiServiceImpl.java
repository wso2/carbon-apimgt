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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.gateway.ApiArtifactApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIArtifactDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class ApiArtifactApiServiceImpl implements ApiArtifactApiService {

    private static final Log log = LogFactory.getLog(ApiArtifactApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    @Override
    public Response getAPIArtifacts(String apiName, String version, String tenantDomain,
                                   MessageContext messageContext) throws APIManagementException {

        tenantDomain = RestApiCommonUtil.getValidateTenantDomain(tenantDomain);
        String api = GatewayUtils.retrieveDeployedAPI(apiName, version, tenantDomain);
        if (StringUtils.isNotEmpty(api)) {
            APIArtifactDTO apiArtifactDTO = new APIArtifactDTO();
            apiArtifactDTO.api(api);
            return Response.ok().entity(apiArtifactDTO).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}

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

package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminServiceProxy;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.rest.api.APIData;

import javax.ws.rs.core.Response;

public class ApiArtifactApiServiceImpl implements ApiArtifactApiService {

    private static final Log log = LogFactory.getLog(ApiArtifactApiServiceImpl.class);

    public Response apiArtifactGet(String apiName, String label, String apiId, MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        GatewayAPIDTO gatewayAPIDTO = inMemoryApiDeployer.getAPIArtifact(apiId, label);
        String definition = null;
        JSONObject responseObj = new JSONObject();

        if (gatewayAPIDTO != null) {
            RESTAPIAdminServiceProxy restapiAdminServiceProxy = new RESTAPIAdminServiceProxy
                    (gatewayAPIDTO.getTenantDomain());
            String qualifiedName = GatewayUtils.getQualifiedApiName(gatewayAPIDTO.getProvider(), gatewayAPIDTO.getName(),
                    gatewayAPIDTO.getVersion());
            try {
                if (restapiAdminServiceProxy.getApi(qualifiedName) != null) {
                    definition = gatewayAPIDTO.getApiDefinition();
                }
            } catch (AxisFault axisFault) {
                log.error("Error in fetching deployed API artifacts from Synapse Configuration." , axisFault);
            }
            responseObj.put("Definition", definition);
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            responseObj.put("Message", "Error");
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}

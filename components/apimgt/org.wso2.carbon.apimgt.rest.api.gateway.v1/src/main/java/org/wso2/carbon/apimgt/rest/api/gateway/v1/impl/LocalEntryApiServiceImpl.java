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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.utils.LocalEntryServiceProxy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

import java.util.Map;

public class LocalEntryApiServiceImpl implements LocalEntryApiService {

    private static final Log log = LogFactory.getLog(LocalEntryApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    public Response localEntryGet(String apiName, String version, String tenantDomain, MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        if (tenantDomain == null) {
            tenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
        }
        GatewayAPIDTO gatewayAPIDTO = null;
        JSONObject responseObj = new JSONObject();
        try {
            Map<String, String> apiAttributes = inMemoryApiDeployer.getGatewayAPIAttributes(apiName, version,
                    tenantDomain);
            String apiId = apiAttributes.get(APIConstants.GatewayArtifactSynchronizer.API_ID);
            String label = apiAttributes.get(APIConstants.GatewayArtifactSynchronizer.LABEL);

            if (label == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(apiName + " is not deployed in the Gateway")
                        .build();
            }
            gatewayAPIDTO = inMemoryApiDeployer.getAPIArtifact(apiId, label);
            if (debugEnabled) {
                log.debug("Retrieved Artifacts for " + apiName + " from eventhub");
            }
        } catch (ArtifactSynchronizerException e) {
            String errorMessage = "Error in fetching artifacts from storage";
            log.error(errorMessage, e);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        if (gatewayAPIDTO != null) {
            try {
                JSONArray localEntryArray = new JSONArray();
                JSONArray UnDeploeydLocalEntryArray = new JSONArray();
                if (gatewayAPIDTO.getLocalEntriesToBeAdd() != null) {
                    LocalEntryServiceProxy localEntryServiceProxy = new
                            LocalEntryServiceProxy(gatewayAPIDTO.getTenantDomain());
                    for (GatewayContentDTO localEntry : gatewayAPIDTO.getLocalEntriesToBeAdd()) {
                        if (localEntryServiceProxy.isEntryExists(localEntry.getName())) {
                            localEntryArray.put(localEntryServiceProxy.getEntry(localEntry.getName()));
                        } else {
                            log.error(localEntry.getName() + " was not deployed in the gateway");
                            UnDeploeydLocalEntryArray.put(localEntry.getContent());
                        }
                    }
                }
                responseObj.put("Deployed Local Entries", localEntryArray);
                responseObj.put("Undeployed Local Entries", UnDeploeydLocalEntryArray);
            } catch (AxisFault e) {
                String errorMessage = "Error in fetching deployed artifacts from Synapse Configuration";
                log.error(errorMessage, e);
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            return Response.serverError().entity("Unexpected error occurred").build();
        }
    }
}

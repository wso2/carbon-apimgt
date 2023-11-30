/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.internal.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.impl.GatewayPolicyArtifactsApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;

@Path("/gateway-policy-artifacts")

@Api(description = "the gateway-policy-artifacts API")

@Produces({ "application/json" })


public class GatewayPolicyArtifactsApi  {

  @Context MessageContext securityContext;

GatewayPolicyArtifactsApiService delegate = new GatewayPolicyArtifactsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all the runtime artifacts for a given API UUID and gateway label", notes = "This will provide access to runtime artifacts in database. ", response = Void.class, tags={ "Retrieving Runtime artifacts for gateway policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of runtime Artifacts", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response gatewayPolicyArtifactsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Gateway policy mapping UUID ")  @QueryParam("policyMappingUuid") String policyMappingUuid,  @ApiParam(value = "**Search condition**.  type of gateway ")  @QueryParam("type") String type,  @ApiParam(value = "**Search condition**.  label associated with the policy mapping ")  @QueryParam("gatewayLabel") String gatewayLabel) throws APIManagementException{
        return delegate.gatewayPolicyArtifactsGet(xWSO2Tenant, policyMappingUuid, type, gatewayLabel, securityContext);
    }
}

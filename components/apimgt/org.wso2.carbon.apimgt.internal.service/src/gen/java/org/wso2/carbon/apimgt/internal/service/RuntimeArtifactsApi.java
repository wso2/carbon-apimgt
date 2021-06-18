/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.RuntimeArtifactsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RuntimeArtifactsApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/runtime-artifacts")

@Api(description = "the runtime-artifacts API")

@Produces({ "application/json" })


public class RuntimeArtifactsApi  {

  @Context MessageContext securityContext;

RuntimeArtifactsApiService delegate = new RuntimeArtifactsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all the runtime artifacts for a given API UUID and gateway label", notes = "This will provide access to runtime artifacts in database. ", response = Void.class, tags={ "Retrieving Runtime artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of runtime Artifacts", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response runtimeArtifactsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Api ID ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "**Search condition**.  label associated with the APIs ")  @QueryParam("gatewayLabel") String gatewayLabel,  @ApiParam(value = "**Search condition**.  type of gateway ")  @QueryParam("type") String type,  @ApiParam(value = "**Search condition**.  name of API ")  @QueryParam("name") String name,  @ApiParam(value = "**Search condition**.  version of API ")  @QueryParam("version") String version) throws APIManagementException{
        return delegate.runtimeArtifactsGet(xWSO2Tenant, apiId, gatewayLabel, type, name, version, securityContext);
    }
}

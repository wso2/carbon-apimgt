/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.TransactionCountDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TransactionCountApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.TransactionCountApiServiceImpl;
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
@Path("/transaction-count")

@Api(description = "the transaction-count API")




public class TransactionCountApi  {

  @Context MessageContext securityContext;

TransactionCountApiService delegate = new TransactionCountApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get transaction count", notes = "This endpoint retrieves the transaction count based on various filter parameters.", response = TransactionCountDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Transaction Records" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Transaction count retrieved successfully", response = TransactionCountDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response transactionCountGet( @NotNull @ApiParam(value = "Start time for the transaction count retrieval",required=true)  @QueryParam("startTime") String startTime,  @NotNull @ApiParam(value = "End time for the transaction count retrieval",required=true)  @QueryParam("endTime") String endTime) throws APIManagementException{
        return delegate.transactionCountGet(startTime, endTime, securityContext);
    }
}

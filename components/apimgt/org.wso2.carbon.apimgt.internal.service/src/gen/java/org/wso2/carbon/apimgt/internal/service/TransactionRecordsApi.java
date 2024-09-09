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

package org.wso2.carbon.apimgt.internal.service;

import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.TransactionRecordDTO;
import org.wso2.carbon.apimgt.internal.service.TransactionRecordsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.TransactionRecordsApiServiceImpl;
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
@Path("/transaction-records")

@Api(description = "the transaction-records API")

@Produces({ "application/json" })


public class TransactionRecordsApi  {

  @Context MessageContext securityContext;

TransactionRecordsApiService delegate = new TransactionRecordsApiServiceImpl();


    @POST
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Insert Transaction Records", notes = "Inserts a list of transaction records into the database.", response = Boolean.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully inserted transaction records", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad request. Invalid input data.", response = Void.class),
        @ApiResponse(code = 500, message = "Internal server error. Failed to insert transaction records.", response = Void.class) })
    public Response insertTransactionRecords(@ApiParam(value = "A list of transaction records to be inserted" ,required=true) List<TransactionRecordDTO> body) throws APIManagementException{
        return delegate.insertTransactionRecords(body, securityContext);
    }
}

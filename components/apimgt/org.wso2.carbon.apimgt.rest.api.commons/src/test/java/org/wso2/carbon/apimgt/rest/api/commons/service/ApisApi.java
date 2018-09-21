/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.commons.service;

import io.swagger.annotations.ApiParam;
import org.wso2.msf4j.Request;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 *Dummy API implementation
 */
@Path("/api/am/publisher/v1.[\\d]+/apis")
@Consumes({"application/json"})
@Produces({"application/json"})
@ApplicationPath("/apis")
public class ApisApi {

    @DELETE
    @Path("/{apiId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response apisApiIdDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. The " +
            "combination of the provider of the API, name of the API and the version is also accepted as a valid API " +
            "ID. Should be formatted as **provider-name-version**. ", required = true) @PathParam("apiId") String apiId
            , @ApiParam(value = "Validator for conditional requests; based on ETag. ") @HeaderParam("If-Match")
                                                String ifMatch
            , @ApiParam(value = "Validator for conditional requests; based on Last Modified header. ") @HeaderParam
            ("If-Unmodified-Since") String ifUnmodifiedSince
            , @Context Request request) {

        return Response.noContent().build();
    }

}

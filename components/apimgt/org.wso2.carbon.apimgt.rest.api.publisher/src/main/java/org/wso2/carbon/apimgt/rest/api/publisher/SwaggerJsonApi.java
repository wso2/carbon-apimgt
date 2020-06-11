/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/swagger.json")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/swagger.json", description = "the swagger.json API")
public class SwaggerJsonApi {

    private static final Log log = LogFactory.getLog(SwaggerJsonApi.class);

    /**
     * Retrieves swagger definition of Publisher REST API and returns
     * 
     * @return swagger definition of Publisher REST API in json format
     */
    @GET
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get Swagger Definition", notes = "Get Swagger Definition of Publisher REST API.", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSwagger Definition is returned."),

            @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource."),

            @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported") })

    public Response swaggerJsonGet() {
        try {
            String definition = IOUtils
                    .toString(RestApiUtil.class.getResourceAsStream("/publisher-api.json"), "UTF-8");
            return Response.ok().entity(definition).build();
        } catch (IOException e) {
            String errorMessage = "Error while retrieving the swagger definition of the Publisher API";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}


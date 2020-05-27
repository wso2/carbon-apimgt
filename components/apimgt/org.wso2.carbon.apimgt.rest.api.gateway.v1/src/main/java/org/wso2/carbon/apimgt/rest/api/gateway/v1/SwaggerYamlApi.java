/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/swagger.yaml")
@Consumes({ "text/yaml" })
@Produces({ "text/yaml" })
@io.swagger.annotations.Api(value = "/swagger.yaml", description = "the swagger.yaml API")
public class SwaggerYamlApi {

    private static final Log log = LogFactory.getLog(SwaggerYamlApi.class);
    private static final String LOCK_STORE_OPENAPI_DEF = "LOCK_STORE_OPENAPI_DEF";
    private String openAPIDef = null;

    /**
     * Retrieves swagger definition of Store REST API and returns
     * 
     * @return swagger definition of Store REST API in yaml format
     */
    @GET
    @Consumes({ "text/yaml" })
    @Produces({ "text/yaml" })
    @io.swagger.annotations.ApiOperation(value = "Get Swagger Definition", notes = "Get Swagger Definition of Store REST API.", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSwagger Definition is returned."),

            @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource."),

            @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported") })

    public Response swaggerYamlGet() throws APIManagementException {
        try {
            if (openAPIDef == null) {
                synchronized (LOCK_STORE_OPENAPI_DEF) {
                    if (openAPIDef == null) {
                        String definition = IOUtils
                                .toString(this.getClass().getResourceAsStream("/gateway-api.yaml"), "UTF-8");
                        openAPIDef = new OAS2Parser().removeExamplesFromSwagger(definition);
                    }
                }
            }
            return Response.ok().entity(openAPIDef).build();
        } catch (IOException e) {
            String errorMessage = "Error while retrieving the swagger definition of the Store API";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}


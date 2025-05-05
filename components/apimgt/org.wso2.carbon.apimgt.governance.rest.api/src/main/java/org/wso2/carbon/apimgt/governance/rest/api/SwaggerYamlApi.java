/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.OAS3Parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

/**
 * This class is used to retrieve the swagger definition of Governance REST API
 */
@Path("/swagger.yaml")
@Consumes({"text/yaml"})
@Produces({"text/yaml"})
@io.swagger.annotations.Api(value = "/swagger.yaml", description = "the swagger.yaml API")
public class SwaggerYamlApi {

    private static final Log log = LogFactory.getLog(SwaggerYamlApi.class);
    private static final Object LOCK_GOV_OPENAPI_DEF = new Object();
    private String openAPIDef = null;

    /**
     * Retrieves swagger definition of Governance REST API and returns
     *
     * @return swagger definition of Governance REST API in yaml format
     */
    @GET
    @Consumes({"text/yaml"})
    @Produces({"text/yaml"})
    @io.swagger.annotations.ApiOperation(value = "Get Swagger Definition", notes = "Get OAS of Governance REST API.",
            response = Void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\n OAS is returned."),

            @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty " +
                    "body because the client has already the latest version of the requested resource."),

            @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe " +
                    "requested media type is not supported")})

    public Response swaggerYamlGet() throws APIManagementException {

        try {
            if (openAPIDef == null) {
                synchronized (LOCK_GOV_OPENAPI_DEF) {
                    if (openAPIDef == null) {
                        try (InputStream defStream = this.getClass()
                                .getClassLoader().getResourceAsStream("governance-api.yaml")) {
                            String definition = IOUtils.toString(defStream, StandardCharsets.UTF_8);
                            openAPIDef = new OAS3Parser().removeExamplesFromOpenAPI(definition);
                        }
                    }
                }
            }

            RESTAPICacheConfiguration restapiCacheConfiguration = APIUtil.getRESTAPICacheConfig();
            if (restapiCacheConfiguration.isCacheControlHeadersEnabled()) {
                CacheControl cacheControl = new CacheControl();
                cacheControl.setMaxAge(restapiCacheConfiguration.getCacheControlHeadersMaxAge());
                cacheControl.setPrivate(true);
                return Response.ok().entity(openAPIDef).cacheControl(cacheControl).build();
            } else {
                return Response.ok().entity(openAPIDef).build();
            }
        } catch (IOException e) {
            String errorMessage = "Error while retrieving the OAS of the Governance API";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
            return Response.serverError().entity("Internal Server Error").build(); // Return proper error response
        }
    }
}


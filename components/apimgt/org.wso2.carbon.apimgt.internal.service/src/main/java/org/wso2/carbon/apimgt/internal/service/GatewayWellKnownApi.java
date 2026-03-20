/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Discovery endpoint for platform gateway connection details.
 * <p>
 * {@code gatewayPath} is the internal REST API base path (no {@code /ws} suffix), e.g. {@code internal/data/v1}.
 * The Universal Gateway client appends {@code /ws} when opening WebSocket connections. Control plane metadata
 * is included for client diagnostics.
 */
@Path("/.well-known")
@Api(value = "Gateway Well-Known", tags = {"Gateway Discovery"})
public class GatewayWellKnownApi {

    private static final Log log = LogFactory.getLog(GatewayWellKnownApi.class);

    /**
     * Control plane type identifier.
     */
    private static final String CONTROL_PLANE_TYPE = "APIM";

    /**
     * Control plane version.
     */
    private static final String CONTROL_PLANE_VERSION = "4.7.0";

    // Response field keys
    private static final String FIELD_GATEWAY_PATH = "gatewayPath";
    private static final String FIELD_CONTROL_PLANE = "controlPlane";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_VERSION = "version";

    /**
     * Returns gateway discovery: {@code gatewayPath} (REST base, no {@code /ws}) and control plane metadata.
     *
     * @return JSON with {@code gatewayPath} and {@code controlPlane}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get gateway discovery information",
            notes = "Returns the internal REST base path (gatewayPath) for gateway connection; clients append /ws "
                    + "for WebSocket registration. Includes control plane metadata.",
            response = Map.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved gateway discovery information"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getGatewayWellKnown() {

        if (log.isDebugEnabled()) {
            log.debug("Gateway well-known endpoint invoked");
        }

        String gatewayPath = buildGatewayPath();

        Map<String, Object> controlPlane = new LinkedHashMap<>();
        controlPlane.put(FIELD_TYPE, CONTROL_PLANE_TYPE);
        controlPlane.put(FIELD_VERSION, CONTROL_PLANE_VERSION);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put(FIELD_GATEWAY_PATH, gatewayPath);
        response.put(FIELD_CONTROL_PLANE, controlPlane);

        return Response.ok(response).build();
    }

    /**
     * Builds {@code gatewayPath}: internal web app base path without a leading slash and without a {@code /ws}
     * suffix (Universal Gateway adds {@code /ws} when dialing WebSockets).
     *
     * @return base path (e.g. {@code internal/data/v1})
     */
    private String buildGatewayPath() {

        // Remove leading slash from INTERNAL_WEB_APP_EP if present for consistency
        String basePath = APIConstants.INTERNAL_WEB_APP_EP;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        if (log.isDebugEnabled()) {
            log.debug("Gateway discovery gatewayPath base: " + basePath);
        }
        return basePath;
    }
}

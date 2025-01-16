/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.wso2.carbon.apimgt.governance.rest.api.HealthzApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.HealthzResponseDTO;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

/**
 * This is the implementation class for the Healthz API.
 */
public class HealthzApiServiceImpl implements HealthzApiService {

    public Response getHealthzLiveness(MessageContext messageContext) {
        HealthzResponseDTO healthzResponseDTO = new HealthzResponseDTO();
        healthzResponseDTO.setStatus("Ok");
        return Response.ok().entity(healthzResponseDTO).build();
    }

    public Response getHealthzReadiness(MessageContext messageContext) {
        HealthzResponseDTO healthzResponseDTO = new HealthzResponseDTO();
        healthzResponseDTO.setStatus("Ok");
        return Response.ok().entity(healthzResponseDTO).build();
    }
}
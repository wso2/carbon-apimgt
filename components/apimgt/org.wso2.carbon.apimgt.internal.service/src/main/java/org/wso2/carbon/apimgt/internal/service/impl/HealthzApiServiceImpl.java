/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.HealthzApiService;
import org.wso2.carbon.apimgt.internal.service.dto.HealthStatusDTO;

import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicInteger;


public class HealthzApiServiceImpl implements HealthzApiService {

    private static final Log log = LogFactory.getLog(HealthzApiServiceImpl.class);
    private final AtomicInteger atomicInteger = new AtomicInteger(5);

    public Response healthzGet(MessageContext messageContext) {
        HealthStatusDTO statusDTO = new HealthStatusDTO();
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            boolean healthy = apiAdmin.checkHealth();
            if (healthy) {
                statusDTO.setStatus(HealthStatusDTO.StatusEnum.AVAILABLE);
                return Response.ok().entity(statusDTO).build();
            } else {
                statusDTO.setStatus(HealthStatusDTO.StatusEnum.UNAVAILABLE);
                return Response.serverError().entity(statusDTO).build();
            }
        } catch (Exception e) {
            if (atomicInteger.get() > 0) {
                log.error("Exception during health-check", e);
                atomicInteger.decrementAndGet();
            }
            statusDTO.setStatus(HealthStatusDTO.StatusEnum.UNAVAILABLE);
            return Response.serverError().entity(statusDTO).build();
        }
    }
}

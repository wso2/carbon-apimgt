/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.ApisApiService;
import org.wso2.carbon.apimgt.internal.service.dto.APIListDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;

import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(String xWSO2Tenant, String apiId, String context, String version, String gatewayLabel, String accept,
                            MessageContext messageContext) throws APIManagementException {

        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        APIListDTO apiListDTO;
        if (StringUtils.isNotEmpty(gatewayLabel)) {
            if (StringUtils.isNotEmpty(apiId)) {
                API api = subscriptionValidationDAO.getApiByUUID(apiId, gatewayLabel, xWSO2Tenant);
                apiListDTO = SubscriptionValidationDataUtil.fromAPIToAPIListDTO(api);
            } else if (StringUtils.isNotEmpty(context) && StringUtils.isNotEmpty(version)) {
                if (!context.startsWith("/t/"+xWSO2Tenant.toLowerCase())){
                    apiListDTO = new APIListDTO();
                }
                API api = subscriptionValidationDAO.getAPIByContextAndVersion(context, version, gatewayLabel);
                apiListDTO = SubscriptionValidationDataUtil.fromAPIToAPIListDTO(api);
            } else {
                // Retrieve API Detail according to Gateway label.
                apiListDTO = SubscriptionValidationDataUtil.fromAPIListToAPIListDTO(
                        subscriptionValidationDAO.getAllApis(xWSO2Tenant, gatewayLabel));
            }
        } else {
            apiListDTO = SubscriptionValidationDataUtil.fromAPIListToAPIListDTO(
                    subscriptionValidationDAO.getAllApis(xWSO2Tenant));
        }
        if (APIConstants.APPLICATION_GZIP.equals(accept)) {
            try {
                File zippedResponse = GZIPUtils.constructZippedResponse(apiListDTO);
                return Response.ok().entity(zippedResponse)
                        .header("Content-Disposition", "attachment").
                                header("Content-Encoding", "gzip").build();
            } catch (APIManagementException e) {
                RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
            }
        } else {
            return Response.ok().entity(apiListDTO).build();
        }
        return null;
    }
}

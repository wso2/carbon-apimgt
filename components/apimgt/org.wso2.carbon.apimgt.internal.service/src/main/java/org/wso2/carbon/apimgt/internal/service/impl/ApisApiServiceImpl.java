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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.DeployedAPIRevision;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.subscription.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.ApisApiService;
import org.wso2.carbon.apimgt.internal.service.dto.APIListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeployedAPIRevisionDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeployedEnvInfoDTO;
import org.wso2.carbon.apimgt.internal.service.dto.UnDeployedAPIRevisionDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.CarbonContext;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(String xWSO2Tenant, String apiId, String context, String version, String gatewayLabel,
            Boolean expand, String accept, MessageContext messageContext) throws APIManagementException {
        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        APIListDTO apiListDTO;
        if (StringUtils.isNotEmpty(gatewayLabel)) {
            if (StringUtils.isNotEmpty(apiId)) {
                API api = subscriptionValidationDAO.getApiByUUID(apiId, gatewayLabel, xWSO2Tenant, expand);
                apiListDTO = SubscriptionValidationDataUtil.fromAPIToAPIListDTO(api);
            } else if (StringUtils.isNotEmpty(context) && StringUtils.isNotEmpty(version)) {
                if (!context.startsWith("/t/" + xWSO2Tenant.toLowerCase())) {
                    apiListDTO = new APIListDTO();
                }
                API api = subscriptionValidationDAO
                        .getAPIByContextAndVersion(context, version, gatewayLabel, expand);
                apiListDTO = SubscriptionValidationDataUtil.fromAPIToAPIListDTO(api);
            } else {
                // Retrieve API Detail according to Gateway label.
                apiListDTO = SubscriptionValidationDataUtil.fromAPIListToAPIListDTO(
                        subscriptionValidationDAO.getAllApis(xWSO2Tenant, gatewayLabel, expand));
            }
        } else {
            apiListDTO = SubscriptionValidationDataUtil.fromAPIListToAPIListDTO(
                    subscriptionValidationDAO.getAllApis(xWSO2Tenant, expand));
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

    public Response deployedAPIRevision(List<DeployedAPIRevisionDTO> deployedAPIRevisionDTOList, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        List<String> revisionUUIDs = new ArrayList<>();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        for (DeployedAPIRevisionDTO deployedAPIRevisionDTO : deployedAPIRevisionDTOList) {
            // get revision uuid
            String revisionUUID = apiProvider.getAPIRevisionUUID(Integer.toString(deployedAPIRevisionDTO.getRevisionId()),
                    deployedAPIRevisionDTO.getApiId());
            if (revisionUUID == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(null).build();
            }
            if (!revisionUUIDs.contains(revisionUUID)) {
                revisionUUIDs.add(revisionUUID);
                Map<String, Environment> environments = APIUtil.getEnvironments(tenantDomain);
                List<DeployedAPIRevision> deployedAPIRevisions = new ArrayList<>();
                for (DeployedEnvInfoDTO deployedEnvInfoDTO : deployedAPIRevisionDTO.getEnvInfo()) {
                    DeployedAPIRevision deployedAPIRevision = new DeployedAPIRevision();
                    deployedAPIRevision.setRevisionUUID(revisionUUID);
                    String environment = deployedEnvInfoDTO.getName();
                    if (environments.get(environment) == null) {
                        RestApiUtil.handleBadRequest("Gateway environment not found: " + environment, log);
                    }
                    deployedAPIRevision.setDeployment(environment);
                    deployedAPIRevision.setVhost(deployedEnvInfoDTO.getVhost());
                    if (StringUtils.isEmpty(deployedEnvInfoDTO.getVhost())) {
                        RestApiUtil.handleBadRequest(
                                "Required field 'vhost' not found in deployment", log
                        );
                    }
                    deployedAPIRevisions.add(deployedAPIRevision);
                }
                apiProvider.addDeployedAPIRevision(deployedAPIRevisionDTO.getApiId(), revisionUUID, deployedAPIRevisions);
            }
        }

        return Response.ok().build();
    }

    @Override
    public Response unDeployedAPIRevision(UnDeployedAPIRevisionDTO unDeployedAPIRevisionDTO, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.removeUnDeployedAPIRevision(unDeployedAPIRevisionDTO.getApiUUID(), unDeployedAPIRevisionDTO.getRevisionUUID(),
                unDeployedAPIRevisionDTO.getEnvironment());
        return Response.ok().build();
    }
}

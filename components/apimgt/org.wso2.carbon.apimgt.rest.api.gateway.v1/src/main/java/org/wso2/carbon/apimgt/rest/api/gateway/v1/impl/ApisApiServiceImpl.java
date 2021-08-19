/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisApiIdGet(String apiId, String tenantDomain, MessageContext messageContext) throws APIManagementException {

        tenantDomain = GatewayUtils.validateTenantDomain(tenantDomain, messageContext);
        SubscriptionDataStore subscriptionDataStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (subscriptionDataStore == null) {
            log.warn("Subscription data store not initialized for " + tenantDomain);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        API api;
        if (StringUtils.isNotEmpty(apiId)) {
            api = subscriptionDataStore.getAPIByUUID(apiId);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO().moreInfo("required parameters " +
                    "missing")).build();
        }
        if (api == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<Subscription> subscriptionsByAPIId = subscriptionDataStore.getSubscriptionsByAPIId(api.getApiId());
        APIInfoDTO apiInfoDTO = GatewayUtils.generateAPIInfo(api, subscriptionsByAPIId, subscriptionDataStore);
        return Response.ok().entity(apiInfoDTO).build();
    }

    @Override
    public Response apisGet(String context, String version, String tenantDomain, MessageContext messageContext) throws APIManagementException {

        tenantDomain = GatewayUtils.validateTenantDomain(tenantDomain, messageContext);
        SubscriptionDataStore subscriptionDataStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (subscriptionDataStore == null) {
            log.warn("Subscription data store not initialized for " + tenantDomain);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (StringUtils.isNotEmpty(context) && StringUtils.isNotEmpty(version)) {
            API api = subscriptionDataStore.getApiByContextAndVersion(context, version);
            if (api == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            APIListDTO apiListDTO = GatewayUtils.generateAPIListDTO(Collections.singletonList(api));
            return Response.ok().entity(apiListDTO).build();
        } else if ((StringUtils.isEmpty(context) && StringUtils.isNotEmpty(version)) || (StringUtils.isNotEmpty(context) && StringUtils.isEmpty(version))) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO().moreInfo("required parameters " +
                    "missing")).build();
        } else {
            List<API> apiList = subscriptionDataStore.getAPIs();
            APIListDTO apiListDTO = GatewayUtils.generateAPIListDTO(apiList);
            return Response.status(Response.Status.OK).entity(apiListDTO).build();
        }
    }
}

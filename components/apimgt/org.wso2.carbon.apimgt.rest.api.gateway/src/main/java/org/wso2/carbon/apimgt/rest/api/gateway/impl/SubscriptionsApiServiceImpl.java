/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.gateway.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.SubscriptionListDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * This class used to retrieve in-memory subscriptions holds in gateway.
 */
public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {

    @Override
    public Response getSubscriptions(String apiUUID, String applicationUUID, String tenantDomain,
                                     MessageContext messageContext) throws APIManagementException {

        tenantDomain = RestApiCommonUtil.getValidateTenantDomain(tenantDomain);
        SubscriptionDataStore tenantSubscriptionStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (tenantSubscriptionStore != null) {
            if (StringUtils.isEmpty(apiUUID) || StringUtils.isEmpty(applicationUUID)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            Subscription subscription = tenantSubscriptionStore.getSubscriptionsByUUIds(apiUUID, applicationUUID);
            if (subscription != null) {
                return Response.ok().entity(toSubscriptionListDto(Arrays.asList(subscription))).build();
            }
        }
        return Response.ok().entity(toSubscriptionListDto(Collections.emptyList())).build();
    }

    private SubscriptionListDTO toSubscriptionListDto(List<Subscription> subscriptionList) {

        List<SubscriptionDTO> subscriptionDTOS = new ArrayList<>();
        for (Subscription subscription : subscriptionList) {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO()
                    .subscriptionId(Integer.parseInt(subscription.getSubscriptionId()))
                    .subscriptionUUID(subscription.getSubscriptionUUId())
                    .subscriptionState(subscription.getSubscriptionState())
                    .appId(subscription.getAppId())
                    .applicationUUID(subscription.getApplicationUUID())
                    .apiId(subscription.getApiId())
                    .apiUUID(subscription.getApiUUID())
                    .policyId(subscription.getPolicyId());
            subscriptionDTOS.add(subscriptionDTO);
        }
        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
        subscriptionListDTO.setList(subscriptionDTOS);
        subscriptionListDTO.setCount(subscriptionDTOS.size());
        return subscriptionListDTO;
    }

}

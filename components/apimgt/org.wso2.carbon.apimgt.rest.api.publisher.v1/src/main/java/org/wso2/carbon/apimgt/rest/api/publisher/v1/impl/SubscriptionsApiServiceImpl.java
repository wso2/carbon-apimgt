/*
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
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.SubscriptionsApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationUsageDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriberInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {

    /**
     * Blocks a subscription
     *
     * @param subscriptionId Subscription identifier
     * @param blockState     block state; either BLOCKED or PROD_ONLY_BLOCKED
     * @param ifMatch        If-Match header value
     * @return 200 response and the updated subscription if subscription block is successful
     */
    public Response blockSubscription(String subscriptionId, String blockState, String ifMatch,
                                      MessageContext messageContext) throws APIManagementException {

        SubscriptionDTO subscriptionDTO = SubscriptionsApiCommonImpl.blockSubscription(subscriptionId, blockState);
        return Response.ok().entity(subscriptionDTO).build();
    }

    /**
     * Retrieves all subscriptions or retrieves subscriptions for a given API Id
     *
     * @param apiId       API identifier
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted subscriptions
     */
    @Override
    public Response getSubscriptions(String apiId, Integer limit, Integer offset, String ifNoneMatch, String query,
                                     MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        SubscriptionListDTO subscriptionListDTO = SubscriptionsApiCommonImpl.getSubscriptions(apiId, limit, offset,
                query, organization);
        return Response.ok().entity(subscriptionListDTO).build();
    }

    /**
     * Get monetization usage data for a subscription
     *
     * @param subscriptionId subscription Id
     * @param messageContext message context
     * @return monetization usage data for a subscription
     */
    @Override
    public Response getSubscriptionUsage(String subscriptionId, MessageContext messageContext)
            throws APIManagementException {

        APIMonetizationUsageDTO apiMonetizationUsageDTO = SubscriptionsApiCommonImpl.getSubscriptionUsage(subscriptionId);
        return Response.ok().entity(apiMonetizationUsageDTO).build();
    }

    /**
     * Unblocks a subscription
     *
     * @param subscriptionId subscription identifier
     * @param ifMatch        If-Match header value
     * @return 200 response and the updated subscription if subscription block is successful
     */
    public Response unBlockSubscription(String subscriptionId, String ifMatch,
                                        MessageContext messageContext) throws APIManagementException {

        SubscriptionDTO subscriptionDTO = SubscriptionsApiCommonImpl.unBlockSubscription(subscriptionId);
        return Response.ok().entity(subscriptionDTO).build();
    }

    @Override
    public Response getSubscriberInfoBySubscriptionId(String subscriptionId, MessageContext messageContext)
            throws APIManagementException {

        SubscriberInfoDTO subscriberInfoDTO = SubscriptionsApiCommonImpl.getSubscriberInfoBySubscriptionId(subscriptionId);
        return Response.ok().entity(subscriberInfoDTO).build();
    }
}

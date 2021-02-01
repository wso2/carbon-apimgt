/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.api.model.webhooks.Topic;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDefaultVersionURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIEndpointURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.TopicMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.utils.APIUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

public class TopicsApiServiceImpl implements TopicsApiService {

    private static final Log log = LogFactory.getLog(TopicsApiServiceImpl.class);

    @Override
    public Response topicsApiIdGet(String apiId, String xWSO2Tenant, MessageContext messageContext) {

        if (StringUtils.isNotEmpty(apiId)) {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
            Set<Topic> topics;

            try {
                APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
                ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, tenantDomain);
                TopicListDTO topicListDTO;
                if (apiTypeWrapper.isAPIProduct()) {
                    topics = apiConsumer.getTopics(apiTypeWrapper.getApiProduct().getUuid());
                } else {
                    topics = apiConsumer.getTopics(apiTypeWrapper.getApi().getUuid());
                }
                topicListDTO = TopicMappingUtil.fromTopicListToDTO(topics);
                return Response.ok().entity(topicListDTO).build();
            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else {
                    RestApiUtil.handleInternalServerError("Failed to get topics of Async API " + apiId, e, log);
                }
            }
        } else {
            RestApiUtil.handleBadRequest("API Id is missing in request", log);
        }
        return null;
    }

    @Override
    public Response topicsSubscriptionsGet(String applicationId, String apiId, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isNotEmpty(applicationId)) {
            String username = RestApiCommonUtil.getLoggedInUsername();
            try {
                APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
                TopicSubscriptionListDTO topicSubscriptionListDTO;
                Set<Subscription> subscriptionSet = apiConsumer.getTopicSubscriptions(applicationId, apiId);
                topicSubscriptionListDTO = TopicMappingUtil.fromSubscriptionListToDTO(subscriptionSet);
                return Response.ok().entity(topicSubscriptionListDTO).build();
            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else {
                    RestApiUtil.handleInternalServerError("Failed to get topic subscriptions of Async API " + apiId, e, log);
                }
            }
        } else {
            RestApiUtil.handleBadRequest("Application Id cannot be empty", log);
        }
        return null;
    }
}

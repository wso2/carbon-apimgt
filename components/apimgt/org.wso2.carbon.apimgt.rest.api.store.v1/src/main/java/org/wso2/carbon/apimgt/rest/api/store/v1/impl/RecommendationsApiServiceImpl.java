/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.rest.api.store.v1.RecommendationsApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class RecommendationsApiServiceImpl implements RecommendationsApiService {

    private static final Log log = LogFactory.getLog(RecommendationsApiService.class);

    public Response recommendationsGet(MessageContext messageContext) {
        RecommendationEnvironment recommendationEnvironment = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getApiRecommendationEnvironment();
        List<JSONObject> recommendedApis = new ArrayList<>();
        JSONObject responseObj = new JSONObject();
        String apiId = null;
        try {
            String userName = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            String requestedTenantDomain = apiConsumer.getRequestedTenant();
            String userTenantDomain = MultitenantUtils.getTenantDomain(userName);

            if (apiConsumer.isRecommendationEnabled(requestedTenantDomain) &&
                    userName != APIConstants.WSO2_ANONYMOUS_USER) {
                int maxRecommendations = recommendationEnvironment.getMaxRecommendations();
                String recommendations = apiConsumer.getApiRecommendations(userName, requestedTenantDomain);

                if (recommendations != null) {
                    JSONObject jsonResponse = new JSONObject(recommendations);
                    JSONArray apiList = jsonResponse.getJSONArray("userRecommendations");

                    for (int i = 0; i < apiList.length(); i++) {
                        try {
                            JSONObject apiObj = apiList.getJSONObject(i);
                            apiId = apiObj.getString("id");
                            ApiTypeWrapper apiWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, userTenantDomain);
                            API api = apiWrapper.getApi();
                            APIIdentifier apiIdentifier = api.getId();
                            boolean isApiSubscribed = apiConsumer.isSubscribed(apiIdentifier, userName);
                            if (!isApiSubscribed && recommendedApis.size() <= maxRecommendations) {
                                JSONObject apiDetails = new JSONObject();
                                apiDetails.put("id", apiId);
                                apiDetails.put("name", apiWrapper.getName());
                                apiDetails.put("avgRating", api.getRating());
                                recommendedApis.add(apiDetails);
                            }
                        } catch (APIManagementException e) {
                            log.debug("Requested API "+ apiId +" is not accessible by the consumer");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred when retrieving recommendations through the rest api: ", e);
        }
        int count = recommendedApis.size();
        responseObj.put("count", count);
        responseObj.put("list", recommendedApis);
        String responseStringObj = String.valueOf(responseObj);
        return Response.ok().entity(responseStringObj).build();
    }
}

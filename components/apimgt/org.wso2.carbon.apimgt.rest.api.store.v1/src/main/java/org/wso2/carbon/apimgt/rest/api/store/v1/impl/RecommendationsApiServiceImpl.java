/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.rest.api.store.v1.RecommendationsApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class RecommendationsApiServiceImpl implements RecommendationsApiService {

    private static final Log log = LogFactory.getLog(RecommendationsApiService.class);

    public Response recommendationsGet(MessageContext messageContext) {
        String responseStringObj = "{}";
        String recommendations;
        APIConsumer apiConsumer;
        try {
            String userName = RestApiUtil.getLoggedInUsername();
            apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            recommendations = apiConsumer.getApiRecommendations(userName);

            if (recommendations != null) {
                JSONObject jsonResponse = new JSONObject(recommendations);
                JSONArray apiList = jsonResponse.getJSONArray("userRecommendations");
                String requestedTenant = jsonResponse.getString("requestedTenantDomain");
                String userId = jsonResponse.getString("user");
                JSONObject responseObj = new JSONObject();
                List<JSONObject> recommendedApis = new ArrayList<>();

                for (int i = 0; i < apiList.length(); i++) {
                    try {
                        JSONObject apiObj = apiList.getJSONObject(i);
                        String apiId = apiObj.getString("id");
                        ApiTypeWrapper apiWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, requestedTenant);
                        API api = apiWrapper.getApi();
                        APIIdentifier apiIdentifier = api.getId();
                        boolean isApiSubscribed = apiConsumer.isSubscribed(apiIdentifier, userId);

                        if (!isApiSubscribed) {
                            JSONObject apiDetails = new JSONObject();
                            apiDetails.put("id", apiId);
                            apiDetails.put("name", apiWrapper.getName());
                            apiDetails.put("avgRating", api.getRating());

                            recommendedApis.add(apiDetails);
                        }
                    } catch (APIManagementException e) {
                        log.error("Error occurred when retrieving api details for the recommended API", e);
                    }
                }
                int count = recommendedApis.size();
                responseObj.put("count", count);
                responseObj.put("list", recommendedApis);
                responseStringObj = String.valueOf(responseObj);
                return Response.ok().entity(responseStringObj).build();
            }
        } catch (Exception e) {
            log.error("Error occurred when retrieving recommendations through the rest api: ", e);
        }
        //todo handle response code handling
        return null;
    }
}

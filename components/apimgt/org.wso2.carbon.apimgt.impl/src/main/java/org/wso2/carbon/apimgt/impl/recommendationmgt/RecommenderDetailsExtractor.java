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

package org.wso2.carbon.apimgt.impl.recommendationmgt;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class RecommenderDetailsExtractor implements RecommenderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RecommenderDetailsExtractor.class);
    private static String streamID = "org.wso2.apimgt.recommendation.event.stream:1.0.0";
    private boolean tenantFlowStarted = false;

    final String ADD_API = "ADD_API";
    final String DELETE_API = "DELETE_API";
    final String ADD_NEW_APPLICATION = "ADD_NEW_APPLICATION";
    final String UPDATED_APPLICATION = "UPDATED_APPLICATION";
    final String DELETE_APPLICATION = "DELETE_APPLICATION";
    final String ADD_USER_CLICKED_API = "ADD_USER_CLICKED_API";
    final String ADD_USER_SEARCHED_QUERY = "ADD_USER_SEARCHED_QUERY";
    final String PUBLISHED_STATUS = "PUBLISHED";
    final String DELETED_STATUS = "DELETED";
    final String ACTION_STRING = "action";
    final String PAYLOAD_STRING = "payload";

    String URL;
    String serverUsername;
    String serverPassword;

    String publishingDetailType;
    org.wso2.carbon.apimgt.api.model.API api;
    String tenantDomain;
    Application application;
    String userId;
    int applicationId;
    ApiTypeWrapper clickedApi;
    String userName;
    String searchQuery;

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, API api,
            String tenantDomain) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = ADD_API;
        this.api = api;
        this.tenantDomain = tenantDomain;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, Application application,
            String userId, int applicationId) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = ADD_NEW_APPLICATION;
        this.application = application;
        this.userId = userId;
        this.applicationId = applicationId;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, Application application) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = UPDATED_APPLICATION;
        this.application = application;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, int applicationId) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = DELETE_APPLICATION;
        this.applicationId = applicationId;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, ApiTypeWrapper clickedApi,
            String userName) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = ADD_USER_CLICKED_API;
        this.clickedApi = clickedApi;
        this.userName = userName;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, String searchQuery,
            String userName) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = ADD_USER_SEARCHED_QUERY;
        this.searchQuery = searchQuery;
        this.userName = userName;
    }

    public void run() {
        try {
            if (publishingDetailType.equals(ADD_API)) {
                publishAPIdetails(api, tenantDomain);
            } else if (publishingDetailType.equals(ADD_NEW_APPLICATION)) {
                publishNewApplication(application, userId, applicationId);
            } else if (publishingDetailType.equals(UPDATED_APPLICATION)) {
                publishUpdatedApplication(application);
            } else if (publishingDetailType.equals(DELETE_APPLICATION)) {
                publishedDeletedApplication(applicationId);
            } else if (publishingDetailType.equals(ADD_USER_CLICKED_API)) {
                publishClickedApi(clickedApi, userName);
            } else if (publishingDetailType.equals(ADD_USER_SEARCHED_QUERY)) {
                publishSearchQueries(searchQuery, userName);
            }
        } catch (IOException e) {
            log.error("When extracting data for the recommendation system !",e);
            e.printStackTrace();
        }
    }

    @Override public void publishAPIdetails(API api, String tenantDomain) throws IOException {
        String apiName = api.getId().getApiName();
        String apiStatus = api.getStatus();
        String apiId = api.getUUID();

        if (apiStatus == null) {
            apiStatus = DELETED_STATUS;
        }
        if (apiStatus.equals(PUBLISHED_STATUS)) {

            String apiDescription = api.getDescription();
            String apiContext = api.getContext();
            String apiTags = api.getTags().toString();
            Set<URITemplate> uriTemplates = api.getUriTemplates();
            ArrayList<String> resources = new ArrayList<String>();

            for (URITemplate uriTemplate : uriTemplates) {
                String resource = uriTemplate.getUriTemplate();
                resources.add(resource);
            }

            JSONObject obj = new JSONObject();
            obj.put("api_id", apiId);
            obj.put("api_name", apiName);
            obj.put("description", apiDescription);
            obj.put("context", apiContext);
            obj.put("tenant", tenantDomain);
            obj.put("tags", apiTags);
            obj.put("resources", resources.toString());

            JSONObject payload = new JSONObject();
            payload.put("action", ADD_API);
            payload.put("payload", obj);
            publishEvent(payload.toString());
        } else {
            JSONObject obj = new JSONObject();
            obj.put("api_name", apiName);
            obj.put("tenant", tenantDomain);

            JSONObject payload = new JSONObject();
            payload.put(ACTION_STRING, DELETE_API);
            payload.put(PAYLOAD_STRING, obj);
            publishEvent(payload.toString());
        }
    }

    @Override public void publishNewApplication(Application application, String userId, int applicationId) {
        String appName = application.getName();
        String appDescription = application.getDescription();

        JSONObject obj = new JSONObject();
        obj.put("user", userId);
        obj.put("application_id", applicationId);
        obj.put("application_name", appName);
        obj.put("application_description", appDescription);

        JSONObject payload = new JSONObject();
        payload.put(ACTION_STRING, ADD_NEW_APPLICATION);
        payload.put(PAYLOAD_STRING, obj);
        publishEvent(payload.toString());
    }

    @Override public void publishUpdatedApplication(Application application) {
        String appName = application.getName();
        String appDescription = application.getDescription();
        int appId = application.getId();

        JSONObject obj = new JSONObject();
        obj.put("application_id", appId);
        obj.put("application_name", appName);
        obj.put("application_description", appDescription);

        JSONObject payload = new JSONObject();
        payload.put(ACTION_STRING, UPDATED_APPLICATION);
        payload.put(PAYLOAD_STRING,obj);
        publishEvent(payload.toString());
    }

    @Override public void publishedDeletedApplication(int appId) {
        JSONObject obj = new JSONObject();
        obj.put("appid", appId);

        JSONObject payload = new JSONObject();
        payload.put(ACTION_STRING, DELETE_APPLICATION);
        payload.put(PAYLOAD_STRING, obj);
        publishEvent(payload.toString());
    }

    @Override public void publishClickedApi(ApiTypeWrapper api, String userName) {
        String apiName = api.getName();
        JSONObject obj = new JSONObject();
        obj.put("user", userName);
        obj.put("api_name", apiName);

        JSONObject payload = new JSONObject();
        payload.put(ACTION_STRING, ADD_USER_CLICKED_API);
        payload.put(PAYLOAD_STRING, obj);
        publishEvent(payload.toString());
    }

    @Override public void publishSearchQueries(String query, String username) {
        query = query.split("&", 2)[0];
        JSONObject obj = new JSONObject();
        obj.put("user", username);
        obj.put("search_query", query);

        JSONObject payload = new JSONObject();
        payload.put(ACTION_STRING, ADD_USER_SEARCHED_QUERY);
        payload.put(PAYLOAD_STRING, obj);
        publishEvent(payload.toString());

    }

    public void publishEvent(String payload) {
        Object[] objects = new Object[] { payload };
        Event event = new Event(streamID, System.currentTimeMillis(),
                null, null, objects);
        try {
            startTenantFlow();
            ServiceReferenceHolder.getInstance().getOutputEventAdapterService()
                    .publish("recommendationEventPublisher", Collections.EMPTY_MAP, event);
        } catch (Exception e){
            log.error("Exception occured when publishing events to recommendation engine", e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    private void startTenantFlow() {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        tenantFlowStarted = true;
    }
}

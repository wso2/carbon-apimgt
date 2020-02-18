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

package org.wso2.carbon.apimgt.impl.recommendationmgt;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.cache.Cache;

public class RecommenderDetailsExtractor implements RecommenderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RecommenderDetailsExtractor.class);
    private static String streamID = "org.wso2.apimgt.recommendation.event.stream:1.0.0";
    private boolean tenantFlowStarted = false;
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    private int applicationId;
    private API api;
    private String userName;
    private String searchQuery;
    private String tenantDomain;
    private String publishingDetailType;
    private Application application;
    private ApiTypeWrapper clickedApi;

    public RecommenderDetailsExtractor(API api, String tenantDomain) {

        this.publishingDetailType = APIConstants.ADD_API;
        this.api = api;
        this.tenantDomain = tenantDomain;
    }

    public RecommenderDetailsExtractor(Application application, String userName, int applicationId) {

        this.publishingDetailType = APIConstants.ADD_NEW_APPLICATION;
        this.application = application;
        this.userName = userName;
        this.applicationId = applicationId;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
    }

    public RecommenderDetailsExtractor(Application application, String tenantDomain) {

        this.publishingDetailType = APIConstants.UPDATED_APPLICATION;
        this.application = application;
        this.tenantDomain = tenantDomain;
    }

    public RecommenderDetailsExtractor(int applicationId, String tenantDomain) {

        this.publishingDetailType = APIConstants.DELETE_APPLICATION;
        this.applicationId = applicationId;
        this.tenantDomain = tenantDomain;
    }

    public RecommenderDetailsExtractor(ApiTypeWrapper clickedApi, String userName) {

        this.publishingDetailType = APIConstants.ADD_USER_CLICKED_API;
        this.clickedApi = clickedApi;
        this.userName = userName;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
    }

    public RecommenderDetailsExtractor(String searchQuery, String userName) {

        this.publishingDetailType = APIConstants.ADD_USER_SEARCHED_QUERY;
        this.searchQuery = searchQuery;
        this.userName = userName;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
    }

    public void run() {

        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            if (isRecommendationEnabled(tenantDomain)) {
                if (APIConstants.ADD_API.equals(publishingDetailType)) {
                    publishAPIdetails(api, tenantDomain);
                } else if (APIConstants.ADD_NEW_APPLICATION.equals(publishingDetailType)) {
                    publishNewApplication(application, userName, applicationId);
                } else if (APIConstants.UPDATED_APPLICATION.equals(publishingDetailType)) {
                    publishUpdatedApplication(application);
                } else if (APIConstants.DELETE_APPLICATION.equals(publishingDetailType)) {
                    publishedDeletedApplication(applicationId);
                } else if (APIConstants.ADD_USER_CLICKED_API.equals(publishingDetailType)) {
                    publishClickedApi(clickedApi, userName);
                } else if (APIConstants.ADD_USER_SEARCHED_QUERY.equals(publishingDetailType)) {
                    publishSearchQueries(searchQuery, userName);
                }
            }
        } catch (IOException e) {
            log.error("When extracting data for the recommendation system !", e);
        }
    }

    @Override
    public void publishAPIdetails(API api, String tenantDomain) throws IOException {

        String apiName = api.getId().getApiName();
        String apiStatus = api.getStatus();
        String apiId = api.getUUID();

        if (apiStatus == null) {
            apiStatus = APIConstants.DELETED_STATUS;
        }
        if (apiStatus.equals(APIConstants.PUBLISHED_STATUS)) {
            String apiDescription = api.getDescription();
            String apiContext = api.getContext();
            String apiTags = api.getTags().toString();
            Set<URITemplate> uriTemplates = api.getUriTemplates();
            List<String> resources = new ArrayList<String>();

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
            payload.put("action", APIConstants.ADD_API);
            payload.put("payload", obj);
            publishEvent(payload.toString());
        } else {
            JSONObject obj = new JSONObject();
            obj.put("api_name", apiName);
            obj.put("tenant", tenantDomain);

            JSONObject payload = new JSONObject();
            payload.put(APIConstants.ACTION_STRING, APIConstants.DELETE_API);
            payload.put(APIConstants.PAYLOAD_STRING, obj);
            publishEvent(payload.toString());
        }
    }

    @Override
    public void publishNewApplication(Application application, String userName, int applicationId) {

        String appName = application.getName();
        String appDescription = application.getDescription();
        String userID = getUserId(userName);

        JSONObject obj = new JSONObject();
        obj.put("user", userID);
        obj.put("application_id", applicationId);
        obj.put("application_name", appName);
        obj.put("application_description", appDescription);

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.ACTION_STRING, APIConstants.ADD_NEW_APPLICATION);
        payload.put(APIConstants.PAYLOAD_STRING, obj);
        publishEvent(payload.toString());
    }

    @Override
    public void publishUpdatedApplication(Application application) {

        String appName = application.getName();
        String appDescription = application.getDescription();
        int appId = application.getId();

        JSONObject obj = new JSONObject();
        obj.put("application_id", appId);
        obj.put("application_name", appName);
        obj.put("application_description", appDescription);

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.ACTION_STRING, APIConstants.UPDATED_APPLICATION);
        payload.put(APIConstants.PAYLOAD_STRING, obj);
        publishEvent(payload.toString());
    }

    @Override
    public void publishedDeletedApplication(int appId) {

        JSONObject obj = new JSONObject();
        obj.put("appid", appId);

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.ACTION_STRING, APIConstants.DELETE_APPLICATION);
        payload.put(APIConstants.PAYLOAD_STRING, obj);
        publishEvent(payload.toString());
    }

    @Override
    public void publishClickedApi(ApiTypeWrapper api, String userName) {

        if (userName != APIConstants.WSO2_ANONYMOUS_USER) {
            String userID = getUserId(userName);
            String apiName = api.getName();
            JSONObject obj = new JSONObject();
            obj.put("user", userID);
            obj.put("api_name", apiName);

            JSONObject payload = new JSONObject();
            payload.put(APIConstants.ACTION_STRING, APIConstants.ADD_USER_CLICKED_API);
            payload.put(APIConstants.PAYLOAD_STRING, obj);
            publishEvent(payload.toString());
        }
    }

    @Override
    public void publishSearchQueries(String query, String username) {

        String userID = getUserId(userName);
        query = query.split("&", 2)[0];
        JSONObject obj = new JSONObject();
        obj.put("user", userID);
        obj.put("search_query", query);

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.ACTION_STRING, APIConstants.ADD_USER_SEARCHED_QUERY);
        payload.put(APIConstants.PAYLOAD_STRING, obj);
        publishEvent(payload.toString());

    }

    public void publishEvent(String payload) {

        Object[] objects = new Object[]{payload};
        Event event = new Event(streamID, System.currentTimeMillis(), null, null, objects);
        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ServiceReferenceHolder.getInstance().getOutputEventAdapterService()
                    .publish(APIConstants.RECOMMENDATIONS_WSO2_EVENT_PUBLISHER, Collections.EMPTY_MAP, event);
            if (log.isDebugEnabled()) {
                log.debug("Event Published for recommendation server with payload " + payload);
            }
        } catch (Exception e) {
            log.error("Exception occurred when publishing events to recommendation engine", e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    private void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
        tenantFlowStarted = false;
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                setTenantDomain(tenantDomain, true);
        tenantFlowStarted = true;
    }

    private String getUserId(String userName) {

        String userID = null;
        try {
            userID = apiMgtDAO.getUserID(userName);
        } catch (APIManagementException e) {
            log.error("Error occurred when getting the userID for user " + userName, e);
        }
        return userID;
    }

    private boolean isRecommendationEnabled(String tenantDomain) {

        RecommendationEnvironment recommendationEnvironment = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getApiRecommendationEnvironment();
        if (recommendationEnvironment != null) {
            if (recommendationEnvironment.isApplyForAllTenants()) {
                return true;
            } else {
                try {
                    JSONObject tenantConfig = null;
                    startTenantFlow(tenantDomain);
                    Cache tenantConfigCache = APIUtil.getCache(
                            APIConstants.API_MANAGER_CACHE_MANAGER,
                            APIConstants.TENANT_CONFIG_CACHE_NAME,
                            APIConstants.TENANT_CONFIG_CACHE_MODIFIED_EXPIRY,
                            APIConstants.TENANT_CONFIG_CACHE_ACCESS_EXPIRY);
                    String cacheName = tenantDomain + "_" + APIConstants.TENANT_CONFIG_CACHE_NAME;
                    if (tenantConfigCache.containsKey(cacheName)) {
                        tenantConfig = (JSONObject) tenantConfigCache.get(cacheName);
                    } else {
                        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                .getTenantId(tenantDomain);
                        APIUtil.loadTenantRegistry(tenantId);
                        Registry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                                .getConfigSystemRegistry(tenantId);
                        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                            APIUtil.loadTenantConf(tenantId);
                        }
                        if (registry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)) {
                            Resource resource = registry.get(APIConstants.API_TENANT_CONF_LOCATION);
                            String content = new String((byte[]) resource.getContent());
                            tenantConfig = new JSONObject(content);
                            tenantConfigCache.put(cacheName, tenantConfig);
                        }
                    }
                    if (tenantConfig.has(APIConstants.API_TENANT_CONF_ENABLE_RECOMMENDATION_KEY)) {
                        Object value = tenantConfig.get(APIConstants.API_TENANT_CONF_ENABLE_RECOMMENDATION_KEY);
                        return Boolean.parseBoolean(value.toString());
                    }
                } catch (RegistryException | UserStoreException | NullPointerException | APIManagementException e) {
                    log.error("Error while retrieving Recommendation config from registry", e);
                } finally {
                    if (tenantFlowStarted) {
                        endTenantFlow();
                    }
                }
            }
        }
        return false;
    }
}

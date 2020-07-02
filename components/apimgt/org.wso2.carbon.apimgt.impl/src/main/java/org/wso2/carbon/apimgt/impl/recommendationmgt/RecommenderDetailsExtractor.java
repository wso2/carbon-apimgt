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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
    private String requestTenantDomain;
    private String publishingDetailType;
    private Application application;
    private ApiTypeWrapper clickedApi;
    private RecommendationEnvironment recommendationEnvironment = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getApiRecommendationEnvironment();

    public RecommenderDetailsExtractor(API api, String tenantDomain) {

        this.publishingDetailType = APIConstants.ADD_API;
        this.api = api;
        this.tenantDomain = tenantDomain;
    }

    public RecommenderDetailsExtractor(Application application, String userName, int applicationId,
                                       String requestedTenant) {

        this.publishingDetailType = APIConstants.ADD_NEW_APPLICATION;
        this.application = application;
        this.applicationId = applicationId;
        this.userName = userName;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
        this.requestTenantDomain = requestedTenant;
    }

    public RecommenderDetailsExtractor(Application application, String userName, String requestedTenant) {

        this.publishingDetailType = APIConstants.UPDATED_APPLICATION;
        this.application = application;
        this.applicationId = application.getId();
        this.userName = userName;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
        this.requestTenantDomain = requestedTenant;
    }

    public RecommenderDetailsExtractor(int applicationId, String userName, String requestedTenant) {

        this.publishingDetailType = APIConstants.DELETE_APPLICATION;
        this.applicationId = applicationId;
        this.userName = userName;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
        this.requestTenantDomain = requestedTenant;
    }

    public RecommenderDetailsExtractor(ApiTypeWrapper clickedApi, String userName, String requestedTenant) {

        this.publishingDetailType = APIConstants.ADD_USER_CLICKED_API;
        this.clickedApi = clickedApi;
        this.userName = userName;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
        this.requestTenantDomain = requestedTenant;
    }

    public RecommenderDetailsExtractor(String searchQuery, String userName, String requestedTenant) {

        this.publishingDetailType = APIConstants.ADD_USER_SEARCHED_QUERY;
        this.searchQuery = searchQuery;
        this.userName = userName;
        this.tenantDomain = MultitenantUtils.getTenantDomain(userName);
        this.requestTenantDomain = requestedTenant;
    }

    public void run() {

        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        startTenantFlow(tenantDomain);
        tenantFlowStarted = true;
        try {
            if (APIUtil.isRecommendationEnabled(tenantDomain)) {
                if (APIConstants.ADD_API.equals(publishingDetailType)) {
                    publishAPIDetails(api, tenantDomain);
                } else if (APIConstants.ADD_NEW_APPLICATION.equals(publishingDetailType)) {
                    publishApplicationDetails(application, userName, applicationId);
                } else if (APIConstants.UPDATED_APPLICATION.equals(publishingDetailType)) {
                    publishApplicationDetails(application, userName, applicationId);
                } else if (APIConstants.DELETE_APPLICATION.equals(publishingDetailType)) {
                    publishDeletedApplication(applicationId);
                } else if (APIConstants.ADD_USER_CLICKED_API.equals(publishingDetailType)) {
                    publishClickedApi(clickedApi, userName);
                } else if (APIConstants.ADD_USER_SEARCHED_QUERY.equals(publishingDetailType)) {
                    publishSearchQueries(searchQuery, userName);
                }

                if (!APIConstants.ADD_API.equals(publishingDetailType) && userName != null
                        && !APIConstants.WSO2_ANONYMOUS_USER.equals(userName) && requestTenantDomain != null) {
                    updateRecommendationsCache(userName, requestTenantDomain);
                }
            }
        } catch (IOException e) {
            log.error("When extracting data for the recommendation system !", e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    @Override
    public void publishAPIDetails(API api, String tenantDomain) throws IOException {

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
            JSONObject swaggerDef = null;
            if (api.getSwaggerDefinition() != null) {
                swaggerDef = new JSONObject(api.getSwaggerDefinition());
            }
            JSONArray resourceArray = new JSONArray();
            JSONObject resourceObj;

            for (URITemplate uriTemplate : uriTemplates) {
                resourceObj = new JSONObject();
                String resource = uriTemplate.getUriTemplate();
                String resourceMethod = uriTemplate.getHTTPVerb();
                resourceObj.put("resource", resource);
                if (swaggerDef != null) {
                    String summary = getDescriptionFromSwagger(swaggerDef, resource, resourceMethod, "summary");
                    String description = getDescriptionFromSwagger(swaggerDef, resource, resourceMethod, "description");
                    resourceObj.put("summary", summary);
                    resourceObj.put("description", description);
                }
                resourceArray.put(resourceObj);
            }

            JSONObject obj = new JSONObject();
            obj.put("api_id", apiId);
            obj.put("api_name", apiName);
            obj.put("description", apiDescription);
            obj.put("context", apiContext);
            obj.put("tenant", tenantDomain);
            obj.put("tags", apiTags);
            obj.put("resources", resourceArray);

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
            log.info(apiName + " API published to recommendation server");
        }
    }

    @Override
    public void publishApplicationDetails(Application application, String userName, int applicationId) {

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
        log.info(appName + " Application published to recommendations server");
    }

    @Override
    public void publishDeletedApplication(int appId) {

        JSONObject obj = new JSONObject();
        obj.put("appid", appId);

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.ACTION_STRING, APIConstants.DELETE_APPLICATION);
        payload.put(APIConstants.PAYLOAD_STRING, obj);
        publishEvent(payload.toString());
        log.info("Delete event for Application id " + appId + " sent to recommendations server");
    }

    @Override
    public void publishClickedApi(ApiTypeWrapper api, String userName) {

        if (userName == null) {
            log.error("Username cannot be null");
            return;
        }
        if (!APIConstants.WSO2_ANONYMOUS_USER.equals(userName)) {
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

        if (username == null) {
            log.error("Username cannot be null");
            return;
        }
        if (!APIConstants.WSO2_ANONYMOUS_USER.equals(userName)) {
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
    }

    public void publishEvent(String payload) {

        Object[] objects = new Object[]{payload};
        Event event = new Event(streamID, System.currentTimeMillis(), null, null, objects);
        APIUtil.publishEvent(APIConstants.RECOMMENDATIONS_WSO2_EVENT_PUBLISHER, Collections.EMPTY_MAP, event);
        if (log.isDebugEnabled()) {
            log.debug("Event Published for recommendation server with payload " + payload);
        }
    }

    private void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                setTenantDomain(tenantDomain, true);
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

    private String getDescriptionFromSwagger(JSONObject swaggerDef, String resource, String resourceMethod,
                                             String keyWord) {

        String description = null;
        try {
            description = (String) swaggerDef.getJSONObject("paths").getJSONObject(resource)
                    .getJSONObject(resourceMethod.toLowerCase()).get(keyWord);
        } catch (JSONException e) {
            log.debug(keyWord + " is not found for " + resource);
        }
        return description;
    }

    /**
     * Update the recommendationsCache by  connecting with the recommendation engine and getting recommendations for
     * the given user for given tenant domain. A user can have several entries cache for different tenants
     *
     * @param userName     User's Name
     * @param tenantDomain tenantDomain
     */
    public void updateRecommendationsCache(String userName, String tenantDomain) {

        long currentTime = System.currentTimeMillis();
        long lastUpdatedTime = 0;
        long waitDuration = recommendationEnvironment.getWaitDuration() * 60 * 1000;
        Cache recommendationsCache = CacheProvider.getRecommendationsCache();
        String cacheName = userName + "_" + tenantDomain;
        JSONObject cachedObject = (JSONObject) recommendationsCache.get(cacheName);
        if (cachedObject != null) {
            lastUpdatedTime = (long) cachedObject.get(APIConstants.LAST_UPDATED_CACHE_KEY);
        }
        if (currentTime - lastUpdatedTime < waitDuration) { // double checked locking to avoid unnecessary locking
            return;
        }
        synchronized (RecommenderDetailsExtractor.class) {
            // Only get recommendations if the last update was was performed more than 15 minutes ago
            if (currentTime - lastUpdatedTime < waitDuration) {
                return;
            }
            String recommendations = getRecommendations(userName, tenantDomain);
            JSONObject object = new JSONObject();
            object.put(APIConstants.RECOMMENDATIONS_CACHE_KEY, recommendations);
            object.put(APIConstants.LAST_UPDATED_CACHE_KEY, System.currentTimeMillis());
            recommendationsCache.put(cacheName, object);
        }
    }

    public String getRecommendations(String userName, String tenantDomain) {

        String recommendationEndpointURL = recommendationEnvironment.getRecommendationServerURL()
                + APIConstants.RECOMMENDATIONS_GET_RESOURCE;
        AccessTokenGenerator accessTokenGenerator = ServiceReferenceHolder.getInstance().getAccessTokenGenerator();
        try {
            String userID = apiMgtDAO.getUserID(userName);
            URL serverURL = new URL(recommendationEndpointURL);
            int serverPort = serverURL.getPort();
            String serverProtocol = serverURL.getProtocol();

            HttpGet method = new HttpGet(recommendationEndpointURL);
            HttpClient httpClient = APIUtil.getHttpClient(serverPort, serverProtocol);
            if (recommendationEnvironment.getOauthURL() != null) {
                String accessToken = accessTokenGenerator.getAccessToken();
                method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                        APIConstants.AUTHORIZATION_BEARER + accessToken);
            } else {
                byte[] credentials = org.apache.commons.codec.binary.Base64.encodeBase64(
                        (recommendationEnvironment.getUserName() + ":" + recommendationEnvironment.getPassword())
                                .getBytes(StandardCharsets.UTF_8));
                method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                        APIConstants.AUTHORIZATION_BASIC + new String(credentials, StandardCharsets.UTF_8));
            }
            method.setHeader(APIConstants.RECOMMENDATIONS_USER_HEADER, userID);
            method.setHeader(APIConstants.RECOMMENDATIONS_ACCOUNT_HEADER, tenantDomain);

            HttpResponse httpResponse = httpClient.execute(method);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info("Recommendations received for the user " + userName + " from recommendations server");
                String contentString = EntityUtils.toString(httpResponse.getEntity());
                if (log.isDebugEnabled()) {
                    log.debug("Recommendations received for user " + userName + " is " + contentString);
                }
                return contentString;
            } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED &&
                    accessTokenGenerator != null){
                log.warn("Error getting recommendations from server. Invalid credentials used");
                accessTokenGenerator.removeInvalidToken(new String[]{APIConstants.OAUTH2_DEFAULT_SCOPE});
            } else {
                log.warn("Error getting recommendations from server. Server responded with "
                        + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.error("Connection failure for the recommendation engine", e);
        } catch (APIManagementException e) {
            log.error("Error while getting recommendations for user " + userName, e);
        }
        return null;
    }
}

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

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import javax.xml.ws.Response;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RecommenderDetailsExtractor implements RecommenderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RecommenderDetailsExtractor.class);

    final String API = "API";
    final String NEW_APP = "NEW_APPLICATION";
    final String UPDATE_APP = "UPDATED_APPLICATION";
    final String DELETE_APP = "DELETE_APPLICATION";
    final String CLICKED_API = "USER_CLICKED_API";
    final String SEARCH_QUERY = "USER_SEARCHED_QUERY";
    final String PUBLISHED_STATUS = "PUBLISHED";
    final String DELETED_STATUS = "DELETED";

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

        this.publishingDetailType = API;
        this.api = api;
        this.tenantDomain = tenantDomain;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, Application application,
            String userId, int applicationId) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = NEW_APP;
        this.application = application;
        this.userId = userId;
        this.applicationId = applicationId;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, Application application) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = UPDATE_APP;
        this.application = application;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, int applicationId) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = DELETE_APP;
        this.applicationId = applicationId;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, ApiTypeWrapper clickedApi,
            String userName) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = CLICKED_API;
        this.clickedApi = clickedApi;
        this.userName = userName;
    }

    public RecommenderDetailsExtractor(RecommendationEnvironment recommendationEnvironment, String searchQuery,
            String userName) {
        this.URL = recommendationEnvironment.getUrl();
        this.serverUsername = recommendationEnvironment.getUsername();
        this.serverPassword = recommendationEnvironment.getPassword();

        this.publishingDetailType = SEARCH_QUERY;
        this.searchQuery = searchQuery;
        this.userName = userName;
    }

    public void run() {
        try {
            if (publishingDetailType.equals(API)) {
                publishAPIdetails(api, tenantDomain);
            } else if (publishingDetailType.equals(NEW_APP)) {
                publishNewApplication(application, userId, applicationId);
            } else if (publishingDetailType.equals(UPDATE_APP)) {
                publishUpdatedApplication(application);
            } else if (publishingDetailType.equals(DELETE_APP)) {
                publishedDeletedApplication(applicationId);
            } else if (publishingDetailType.equals(CLICKED_API)) {
                publishClickedApi(clickedApi, userName);
            } else if (publishingDetailType.equals(SEARCH_QUERY)) {
                publishSearchQueries(searchQuery, userName);
            }

        } catch (IOException e) {
            log.info("[Error] When extracting data for the recommendation system !");
            e.printStackTrace();
        }
    }

    public void sendPostRequest(String URL, String payload) {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(URL);
        post.setHeader("Content-Type", "application/json ");
        try {
            byte[] encodedAuth = Base64
                    .encodeBase64((serverUsername + ":" + serverPassword).getBytes(Charset.forName("ISO-8859-1")));
            String authHeader = "Basic " + new String(encodedAuth);
            post.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            post.setEntity(new StringEntity(payload));
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK){
                log.info(String.valueOf(statusCode));
            }else {
                log.error(String.valueOf(statusCode));
            }
        } catch (Exception e) {
            log.error(e+"[ERROR] Connection failure for the recommendation engine");
        }
    }

    public void sendDeleteRequest(String URL) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(URL);
        try {
            byte[] encodedAuth = Base64
                    .encodeBase64((serverUsername + ":" + serverPassword).getBytes(Charset.forName("ISO-8859-1")));
            String authHeader = "Basic " + new String(encodedAuth);
            httpDelete.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            httpclient.execute(httpDelete);

        } catch (IOException e) {
            log.info("[ERROR] Connection failure for the recommendation engine");
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

            String payload = "{\"api_id\":\"" + apiId + "\",\"api_name\":\"" + apiName + "\", " + "\"description\":\""
                    + apiDescription + "\",\"context\":\"" + apiContext + "\"," + "\"tenant\":\"" + tenantDomain
                    + "\",\"tags\":\"" + apiTags + "\"," + "\"resources\":\"" + resources + "\"}";
            payload = payload.replaceAll("[\n\t]", " ");

            String url = URL + "addapi";
            sendPostRequest(url, payload);

        } else {
            String url = URL + "deleteapi?api=" + apiName + "&tenant=" + tenantDomain;
            sendDeleteRequest(url);
        }
    }

    @Override public void publishNewApplication(Application application, String userId, int applicationId) {

        String appName = application.getName();
        String appDescription = application.getDescription();

        String payload =
                "{\"user\":\"" + userId + "\",\"application_id\":\"" + applicationId + "\",\"application_name\":\""
                        + appName + "\"," + "\"application_description\":\"" + appDescription + "\"}";
        payload = payload.replaceAll("[\n\t]", " ");

        String url = URL + "addapplication";
        sendPostRequest(url, payload);
    }

    @Override public void publishUpdatedApplication(Application application) {

        String appName = application.getName();
        String appDescription = application.getDescription();
        int appId = application.getId();

        String payload =
                "{\"application_id\":\"" + appId + "\",\"application_name\":\"" + appName + "\"," + "\"application_description\":\""
                        + appDescription + "\"}";
        payload = payload.replaceAll("[\n\t]", " ");

        String url = URL + "updateapplication";
        sendPostRequest(url, payload);
    }

    @Override public void publishedDeletedApplication(int appId) {
        String url = URL + "deleteapplication?appid=" + appId;
        sendDeleteRequest(url);
    }

    @Override public void publishClickedApi(ApiTypeWrapper api, String userName) {
        String apiName = api.getName();
        String payload = "{\"user\":\"" + userName + "\", \"api_name\":\"" + apiName + "\"}";
        payload = payload.replaceAll("[\n\t]", " ");

        String url = URL + "addclickedapi";
        sendPostRequest(url, payload);
    }

    @Override public void publishSearchQueries(String query, String username) {
        query = query.split("&", 2)[0];
        String payload = "{\"user\":\"" + username + "\", \"search_query\":\"" + query + "\"}";
        payload = payload.replaceAll("[\n\t]", " ");

        String url = URL + "addsearchquery";
        sendPostRequest(url, payload);

    }
}

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.wso2.carbon.apimgt.usage.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.usage.client.bean.FirstAccessRequestSearchBean;
import org.wso2.carbon.apimgt.usage.client.bean.RequestSearchBean;
import org.wso2.carbon.apimgt.usage.client.bean.Result;
import org.wso2.carbon.apimgt.usage.client.bean.TableExistResponseBean;
import org.wso2.carbon.apimgt.usage.client.util.RestClientUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class DASRestClient {
    private DefaultHttpClient httpClient;
    private String dasUrl;
    private String user;
    private String pass;
    private final Gson gson = new Gson();
    private static final Log log = LogFactory.getLog(DASRestClient.class);

    public DASRestClient(String url, String user, String pass) {
        httpClient = new DefaultHttpClient();
        this.dasUrl = url;
        this.user = user;
        this.pass = pass;

    }

    HttpResponse post(String js, String url) throws IOException {
        httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);
        String cred = RestClientUtil.encodeCredentials(this.user, this.pass);
        postRequest.addHeader(APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_NAME,
                APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_TYPE + ' ' + cred);
        StringEntity input = new StringEntity(js);
        input.setContentType(APIUsageStatisticsClientConstants.APPLICATION_JSON);
        postRequest.setEntity(input);

        HttpResponse response;
        try {
            response = httpClient.execute(postRequest);
        } finally {
//            httpClient.getConnectionManager().shutdown();
        }

        return response;

    }

    <T> List<Result<T>> parse(HttpResponse response, Type type) throws IllegalStateException, IOException {

        BufferedReader reader = null;
        List<Result<T>> obj;
        try {
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            obj = gson.fromJson(reader, type);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //this is logged and the process is continued because parsing is done
                    log.error("Error occurred while closing the buffers reader.", e);
                }
            }
        }
        return obj;
    }

    public <T> List<Result<T>> sendAndGetPost(RequestSearchBean request, Type type)
            throws JsonSyntaxException, IOException {

        String json = gson.toJson(request);
        HttpResponse response = post(json,
                dasUrl + APIUsageStatisticsClientConstants.DAS_AGGREGATES_SEARCH_REST_API_URL);

        List<Result<T>> result = parse(response, type);

        return result;
    }

    public <T> List<Result<T>> sendAndGetPost(FirstAccessRequestSearchBean request, Type type)
            throws JsonSyntaxException, IOException {

        String json = gson.toJson(request);
        HttpResponse response = post(json, dasUrl + APIUsageStatisticsClientConstants.DAS_SEARCH_REST_API_URL);

        List<Result<T>> result = parse(response, type);

        return result;
    }

    public TableExistResponseBean isTableExist(String name) throws JsonSyntaxException, IOException {

        HttpGet getRequest = new HttpGet(
                dasUrl + APIUsageStatisticsClientConstants.DAS_Table_EXIST_REST_API_URL + "?table=" + name);
        String cred = RestClientUtil.encodeCredentials(this.user, this.pass);
        getRequest.addHeader(APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_NAME,
                APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_TYPE + ' ' + cred);
        TableExistResponseBean obj = null;
        BufferedReader reader = null;
        try {
            HttpResponse response = httpClient.execute(getRequest);

            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            Type type = new TypeToken<TableExistResponseBean>() {
            }.getType();

            obj = gson.fromJson(reader, type);
        } finally {
//            httpClient.getConnectionManager().shutdown();

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //this is logged and the process is continued because parsing is done
                    log.error("Error occurred while closing the buffers reader.", e);
                }
            }

        }
        return obj;

    }

}

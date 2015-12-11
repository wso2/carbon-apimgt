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
import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.bean.RequestSearchBean;
import org.wso2.carbon.apimgt.usage.client.bean.Result;
import org.wso2.carbon.apimgt.usage.client.bean.SearchRequestBean;
import org.wso2.carbon.apimgt.usage.client.bean.TableExistResponseBean;
import org.wso2.carbon.apimgt.usage.client.util.RestClientUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contain the actual Client side implementation for the DAS rest client.
 */
public class DASRestClient {
    private CloseableHttpClient httpClient;
    private String dasUrl;
    private String user;
    private String pass;
    private final Gson gson = new Gson();
    private static final Log log = LogFactory.getLog(DASRestClient.class);

    /**
     * get instance providing DAS configuration
     *
     * @param url  DAS rest api location
     * @param user DAS rest api username
     * @param pass DAs rest api password
     */
    public DASRestClient(String url, String user, String pass) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();       
       
        URL dasURL = new URL(url);      
        //httpClient = HttpClients.custom().setConnectionManager(cm).build();
        httpClient = (CloseableHttpClient) APIUtil.getHttpClient(dasURL.getPort(), dasURL.getProtocol());
        this.dasUrl = url;
        this.user = user;
        this.pass = pass;

    }

    /**
     * Do a post request to the DAS REST
     *
     * @param json lucene json request
     * @param url  DAS rest api location
     * @return return the HttpResponse after the request sent
     * @throws IOException throw if the connection exception occur
     */
    CloseableHttpResponse post(String json, String url) throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("Sending Lucene Query : " + json);
        }

        HttpPost postRequest = new HttpPost(url);
        HttpContext context = HttpClientContext.create();

        //get the encoded basic authentication
        String cred = RestClientUtil.encodeCredentials(this.user, this.pass);
        postRequest.addHeader(APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_NAME,
                APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_TYPE + ' ' + cred);
        StringEntity input = new StringEntity(json);
        input.setContentType(APIUsageStatisticsClientConstants.APPLICATION_JSON);
        postRequest.setEntity(input);

        CloseableHttpResponse response;
        try {
            //send the request
            response = httpClient.execute(postRequest, context);
        } finally {
            //            httpClient.getConnectionManager().shutdown();
        }

        return response;

    }

    /**
     * Generic method to parse the response to the given class type
     *
     * @param response HttpResponse came form the HTTP request
     * @param type     type of the class that need to parse
     * @param <T>      class of the expected class
     * @return return list of Result having indicated objects
     * @throws IllegalStateException throws if httpclient face and illegal state
     * @throws IOException           throws if connection error occur
     */
    <T> List<Result<T>> parse(CloseableHttpResponse response, Type type) throws IllegalStateException, IOException {

        BufferedReader reader = null;
        List<Result<T>> obj;
        try {
            //get the buffed reader form the HttpResponse
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            //conversion from the json to JAVA objects
            obj = gson.fromJson(reader, type);
        } finally {
            if (reader != null) {
                try {
                    //close the buffered reader
                    reader.close();
                } catch (IOException e) {
                    //this is logged and the process is continued because parsing is done
                    log.error("Error occurred while closing the buffers reader.", e);
                }
            }

            if (response != null) {
                try {
                    //close the response reader
                    response.close();
                } catch (IOException e) {
                    //this is logged and the process is continued because parsing is done
                    log.error("Error occurred while closing the response.", e);
                }
            }

        }
        return obj;
    }

    /**
     * Top level method to make post request and return java object type response for the general aggregate search requests
     *
     * @param request SearchRequestBean representing the lucene json object for search records
     * @param type    type of the expected java object type
     * @param <T>     expected values of the Result object
     * @return return list of Result objects containing the <T> values
     * @throws JsonSyntaxException throws if error occur parsing response back to the java
     * @throws IOException         throws if connection error occur to the REST API
     */
    public <T> List<Result<T>> doPost(SearchRequestBean request, Type type) throws JsonSyntaxException, IOException {

        //get the json string of the request object
        String json = gson.toJson(request);

        //doing a post request on the aggregate REST API
        CloseableHttpResponse response = post(json,
                dasUrl + APIUsageStatisticsClientConstants.DAS_AGGREGATES_SEARCH_REST_API_URL);

        //check the status code of the response
        if (response.getStatusLine().getStatusCode() == 500) {
            log.warn("DAS internal Server Error, Table '" + request.getTableName() + "' may not contain any Records.");
            return new ArrayList<Result<T>>();
        }

        //parse the response back to the java objects
        List<Result<T>> result = parse(response, type);

        return result;
    }

    /**
     * Top level method to make post request and return java object type response for first access time requests
     *
     * @param request RequestSearchBean representing the lucene json object for first access time search
     * @param type    type of the expected java object type
     * @param <T>     expected values of the Result object
     * @return return list of Result objects containing the <T> values
     * @throws JsonSyntaxException throws if error occur parsing response back to the java
     * @throws IOException         throws if connection error occur to the REST API
     */
    public <T> List<Result<T>> doPost(RequestSearchBean request, Type type)
            throws JsonSyntaxException, IOException {
        //get the json string of the request object
        String json = gson.toJson(request);

        //doing a post request on the Search REST API
        CloseableHttpResponse response = post(json, dasUrl + APIUsageStatisticsClientConstants.DAS_SEARCH_REST_API_URL);

        //parse the response back to the java objects
        List<Result<T>> result = parse(response, type);

        return result;
    }

    /**
     * use to check provided Table is present in the DAS Data access layer
     *
     * @param name Table name
     * @return TableExistResponseBean which contain the row response from the REST API
     * @throws JsonSyntaxException throws if parsing error occur
     * @throws IOException         throws if connection problem occur
     */
    public TableExistResponseBean isTableExist(String name) throws JsonSyntaxException, IOException {

        //crete the http get request method to RETS API
        HttpGet getRequest = new HttpGet(
                dasUrl + APIUsageStatisticsClientConstants.DAS_TABLE_EXIST_REST_API_URL + "?table=" + name);

        //get the encoded REST API credentials
        String cred = RestClientUtil.encodeCredentials(this.user, this.pass);
        getRequest.addHeader(APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_NAME,
                APIUsageStatisticsClientConstants.HTTP_AUTH_HEADER_TYPE + ' ' + cred);
        TableExistResponseBean obj = null;
        BufferedReader reader = null;
        try {
            //get the response
            HttpResponse response = httpClient.execute(getRequest);

            //get the buffered reader
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            //get the expected result type
            Type type = new TypeToken<TableExistResponseBean>() {
            }.getType();

            //pass to java object
            obj = gson.fromJson(reader, type);
        } finally {
            //            httpClient.getConnectionManager().shutdown();

            if (reader != null) {
                try {
                    //close the reader when done
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

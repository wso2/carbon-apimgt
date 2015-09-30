package org.wso2.carbon.apimgt.usage.client.util;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wso2.carbon.apimgt.usage.client.bean.FirstAccessRequestSearchBean;
import org.wso2.carbon.apimgt.usage.client.bean.RequestSearchBean;
import org.wso2.carbon.apimgt.usage.client.bean.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by rukshan on 9/29/15.
 */
public class DASRestClient {
    DefaultHttpClient httpClient;
    Gson gson;
    public DASRestClient() {
        httpClient = new DefaultHttpClient();
        gson = new Gson();
    }

    HttpResponse post(String js) throws Exception {
        HttpPost postRequest = new HttpPost(
                "https://localhost:9445/analytics/aggregates");
        postRequest.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
        StringEntity input = new StringEntity(js);
        input.setContentType("application/json");
        postRequest.setEntity(input);
        return httpClient.execute(postRequest);
    }

    HttpResponse postS(String js) throws Exception {
        HttpPost postRequest = new HttpPost(
                "https://localhost:9445/analytics/search");
        postRequest.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
        StringEntity input = new StringEntity(js);
        input.setContentType("application/json");
        postRequest.setEntity(input);
        return httpClient.execute(postRequest);
    }

    <T> List<Result<T>> parse(HttpResponse response, Type ty)
            throws IllegalStateException, IOException {
        BufferedReader re = new BufferedReader(new InputStreamReader(response
                .getEntity().getContent()));
        List<Result<T>> obj = gson.fromJson(re, ty);
        return obj;
    }

    public <T> List<Result<T>> sendAndGetPost(RequestSearchBean request, Type ty){

        String json=gson.toJson(request);
        HttpResponse response;
        try {
            response=post(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        List<Result<T>> result= null;
        try {
            result = parse(response,ty);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public <T> List<Result<T>> sendAndGetPost(FirstAccessRequestSearchBean request, Type ty) {
        // System.out.println(gson);
        String json = gson.toJson(request);
        HttpResponse response;
        try {
            response = postS(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        List<Result<T>> result = null;
        try {
            result = parse(response, ty);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}

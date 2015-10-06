package org.wso2.carbon.apimgt.usage.client.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
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
    String dasUrl="https://localhost:9445";

    Gson gson;
    public DASRestClient() {
        httpClient = new DefaultHttpClient();
        gson = new Gson();
    }

    HttpResponse post(String js,String url) throws IOException {
        httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);
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
        EntityUtils.consume(response.getEntity());
        return obj;
    }

    public <T> List<Result<T>> sendAndGetPost(RequestSearchBean request, Type ty) throws JsonSyntaxException,IOException {

        String json=gson.toJson(request);
        HttpResponse response= post(json, dasUrl + RESTClientConstant.DAS_AGGREGATES_SEARCH_REST_API_URL);

        List<Result<T>> result= parse(response,ty);

        return result;
    }

    public <T> List<Result<T>> sendAndGetPost(FirstAccessRequestSearchBean request, Type ty) throws JsonSyntaxException,IOException {

        String json = gson.toJson(request);
        HttpResponse response = post(json,dasUrl+RESTClientConstant.DAS_SEARCH_REST_API_URL);

        List<Result<T>> result = parse(response, ty);


        return result;
    }

    public TableExistResponseBean isTableExist(String name) throws JsonSyntaxException, IOException {
        HttpGet getRequest = new HttpGet(dasUrl+RESTClientConstant.DAS_Table_EXIST_REST_API_URL+"?table="+name);
        getRequest.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");

        HttpResponse response= httpClient.execute(getRequest);

        BufferedReader re = new BufferedReader(new InputStreamReader(response
                .getEntity().getContent()));
        Type ty = new TypeToken<TableExistResponseBean>() {
        }.getType();

        TableExistResponseBean obj = gson.fromJson(re,ty);
        EntityUtils.consume(response.getEntity());

        return obj;

    }

}

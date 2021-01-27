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
 */

package org.wso2.carbon.apimgt.persistence.mongodb;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.mongodb.utils.MongoDBConnectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MongoDBAtlasAPIConnector {

    private static String database = null;
    private static final String DEFAULT_DATABASE = "APIM_DB";
    private static MongoDBAtlasAPIConnector instance = null;
    private static final Log log = LogFactory.getLog(MongoDBAtlasAPIConnector.class);
    private static String apiUri;
    private static String groupId;
    private static String clusterName;
    private static String publicKey;
    private static String privateKey;

    public MongoDBAtlasAPIConnector() {
        Map<String, String> persistenceConfigs = ServiceReferenceHolder.getInstance().getPersistenceConfigs();
        database = MongoDBConnectionUtil.getDatabase().getName();
        apiUri = persistenceConfigs.get("RegistryConfigs.APIUri");
        groupId = persistenceConfigs.get("RegistryConfigs.GroupId");
        clusterName = persistenceConfigs.get("RegistryConfigs.ClusterName");
        publicKey = persistenceConfigs.get("RegistryConfigs.PublicKey");
        privateKey = persistenceConfigs.get("RegistryConfigs.PrivateKey");
    }

    public static MongoDBAtlasAPIConnector getInstance() {
        if (instance == null) {
            instance = new MongoDBAtlasAPIConnector();
        }
        return instance;
    }

    public Boolean createSearchIndexes(String collectionName) {
        URI uri = URI.create(apiUri + "/groups/" + groupId + "/clusters/" + clusterName + "/fts/indexes/");
        HttpPost post = new HttpPost(uri);
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mappings = new HashMap<>();

        mappings.put("dynamic", true);
        map.put("collectionName", collectionName);
        if (database != null) {
            map.put("database", database);
        } else {
            map.put("database", DEFAULT_DATABASE);
        }
        map.put("mappings", mappings);
        map.put("name", "default");
        PoolingHttpClientConnectionManager connManager
                = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(5);
        connManager.setMaxTotal(5);
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(connManager)
                .build();
        try {
            StringEntity stringEntity = new StringEntity(gson.toJson(map));
            stringEntity.setContentType("application/json");
            post.setEntity(stringEntity);
            Header authHeader = authenticateAPI(post, client, uri);
            post.addHeader("content-type", "application/json");
            post.addHeader(authHeader.getName(), authHeader.getValue());
            HttpResponse execute = client.execute(post);
            int statusCode = execute.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                log.info("Created atlas search index with status " + statusCode);
                return true;
            } else {
                // add payload
                log.error("Failed to create atlas search index with status " + statusCode);
                return false;
            }

        } catch (IOException e) {
            log.error("Error when parsing to json ", e);
        } catch (MalformedChallengeException e) {
            log.error("Error when setting digest auth challenge for atlas api ", e);
        } catch (AuthenticationException e) {
            log.error("Error when authenticating mongodb atlas api ", e);
        }
        return false;
    }

    public JSONArray getIndexes(String collectionName) {
        URI uri = URI.create(apiUri + "/groups/" + groupId + "/clusters/" + clusterName +
                "/fts/indexes/" + database + "/" + collectionName);
        JSONArray jsonArray = null;
        PoolingHttpClientConnectionManager connManager
                = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(5);
        connManager.setMaxTotal(5);
        HttpGet get = new HttpGet(uri);
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(connManager)
                .build();

        try {
            Header authHeader = authenticateAPI(get, client, uri);
            get.addHeader("content-type", "application/json");
            get.addHeader(authHeader.getName(), authHeader.getValue());
            HttpResponse execute = client.execute(get);

            InputStream content = execute.getEntity().getContent();
            JSONParser jsonParser = new JSONParser();
            jsonArray = (JSONArray) jsonParser.parse(new InputStreamReader(content, "UTF-8"));

        } catch (IOException | ParseException e) {
            log.error("Error when parsing to json ", e);
        } catch (MalformedChallengeException e) {
            log.error("Error when setting digest auth challenge for atlas api ", e);
        } catch (AuthenticationException e) {
            log.error("Error when authenticating mongodb atlas api ", e);
        }
        return jsonArray;

    }

    private Header authenticateAPI(HttpRequest request, HttpClient client, URI uri)
            throws MalformedChallengeException, AuthenticationException, IOException {
        DigestScheme md5Auth = new DigestScheme();
        HttpResponse authResponse = client.execute(new HttpGet(uri));
        Header challenge = authResponse.getHeaders("WWW-Authenticate")[0];
        md5Auth.processChallenge(challenge);
        Header authHeader = md5Auth.authenticate(
                new UsernamePasswordCredentials(publicKey, privateKey),
                request,
                new BasicHttpContext()
        );

        md5Auth.createCnonce();
        return authHeader;
    }
}

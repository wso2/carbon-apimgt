/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.CachableResponse;
import org.wso2.carbon.apimgt.api.VectorDBProviderService;
import org.wso2.carbon.apimgt.api.dto.VectorDBProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class ZillizVectorDBProviderServiceImpl implements VectorDBProviderService {
    private static final Log log = LogFactory.getLog(ZillizVectorDBProviderServiceImpl.class);

    private String uri;
    private String token;
    private String collectionName;
    private int dimension;
    private int ttl;
    private final Gson gson = new Gson();

    /**
     * Initialize the Zilliz Vector DB provider with configuration.
     */
    @Override
    public void init(VectorDBProviderConfigurationDTO providerConfig) throws APIManagementException {
        log.debug("Initializing Zilliz Vector DB provider");
        uri = providerConfig.getProperties().get(APIConstants.AI.VECTOR_DB_PROVIDER_URI);
        token = providerConfig.getProperties().get(APIConstants.AI.VECTOR_DB_PROVIDER_TOKEN);

        if (uri == null || token == null) {
            throw new IllegalArgumentException(
                    "Missing required Zilliz configuration: 'uri' or 'token'");
        }
        ttl = Integer.parseInt(providerConfig.getProperties().getOrDefault(APIConstants.AI.VECTOR_DB_PROVIDER_TTL,
                APIConstants.AI.VECTOR_DB_PROVIDER_TTL_DEFAULT));

        log.info("Initializing Zilliz REST client with URI: " + uri);
    }

    @Override public String getType() { return APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_TYPE; }

    /**
     * Create a vector index in the database if it does not exist.
     */
    @Override
    public void createIndex(Map<String, String> providerConfig) throws APIManagementException {
        log.info("Creating Zilliz vector index");
        try {
            // Check if collection exists
            String checkUrl = uri + APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_HAS_COLLECTION_ENDPOINT;
            dimension = Integer.parseInt(providerConfig.
                    get(APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING_DIMENSION));

            collectionName = APIConstants.AI.VECTOR_INDEX_PREFIX + dimension;
            log.debug("Checking if collection exists: " + collectionName);

            JSONObject checkPayload = new JSONObject();
            checkPayload.put(APIConstants.AI.VECTOR_DB_PROVIDER_COLLECTION_NAME, collectionName);

            try (CloseableHttpResponse checkResponse = APIUtil.invokeZillizAPI(checkUrl, token, checkPayload.toString())) {
                int checkStatusCode = checkResponse.getStatusLine().getStatusCode();
                String responseStr = EntityUtils.toString(checkResponse.getEntity());
                JSONObject checkObj = new JSONObject(responseStr);
                if (checkStatusCode == HttpStatus.SC_OK) {
                    boolean exists = checkObj.optJSONObject(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA).
                            optBoolean(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_HAS);
                    if (exists) {
                        log.info("Collection already exists: " + collectionName);
                        return;
                    }
                } else {
                    throw new APIManagementException("Unexpected status code " + checkStatusCode + ": " + responseStr);
                }
            }

            // Create schema for collection
            JSONObject schema = getSchema();

            // Create index for embedding field
            JSONArray indexParams = new JSONArray();

            JSONObject embeddingIndex = new JSONObject();
            embeddingIndex.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FIELD_NAME,
                    APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING);
            embeddingIndex.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_INDEX_TYPE,
                    APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_HNSW);
            embeddingIndex.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_INDEX_NAME, collectionName +
                    APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_INDEX_SUFFIX);
            embeddingIndex.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_METRIC_TYPE,
                    APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_L2);
            JSONObject extraParams = new JSONObject();
            extraParams.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_M, 64);
            extraParams.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_EF_CONSTRUCTION, 100);
            embeddingIndex.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_EXTRA_PARAMS, extraParams);
            indexParams.put(embeddingIndex);

            // Add TTL
            JSONObject params = new JSONObject();
            params.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_TTL_SECONDS, ttl);

            // Build payload
            JSONObject createPayload = new JSONObject();
            createPayload.put(APIConstants.AI.VECTOR_DB_PROVIDER_COLLECTION_NAME, collectionName);
            createPayload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_SCHEMA, schema);
            createPayload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_PARAMS, params);
            createPayload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_INDEX_PARAMS, indexParams);

            String createUrl = uri + APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_CREATE_COLLECTION_ENDPOINT;

            try (CloseableHttpResponse createResponse = APIUtil.invokeZillizAPI(createUrl, token, createPayload.toString())) {
                int createStatusCode = createResponse.getStatusLine().getStatusCode();
                String createResponseStr = EntityUtils.toString(createResponse.getEntity());
                if (createStatusCode != HttpStatus.SC_OK) {
                    if (createStatusCode == HttpStatus.SC_CONFLICT) {
                        log.info("Collection already exists: " + collectionName);
                    } else {
                        String errorMsg = "Failed to create collection: " + createResponseStr;
                        log.error(errorMsg);
                        throw new APIManagementException(errorMsg);
                    }
                } else {
                    log.info("Successfully created collection: " + collectionName);
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error creating Zilliz index: " + e.getMessage(), e);
        }
    }

    @NotNull
    private JSONObject getSchema() {
        JSONObject schema = new JSONObject();
        schema.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_AUTO_ID, false);
        schema.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_ENABLE_DYNAMIC_FIELD, false);

        JSONArray fields = new JSONArray();

        // id field (primary key)
        JSONObject idField = new JSONObject();
        idField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FIELD_NAME, APIConstants.AI.VECTOR_DB_PROVIDER_ID);
        idField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA_TYPE,
                APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_VARCHAR);
        idField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_IS_PRIMARY, true);
        JSONObject idParams = new JSONObject();
        idParams.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_MAX_LENGTH, 36);
        idField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_ELEMENT_TYPE_PARAMS, idParams);
        fields.put(idField);

        // created_at field
        JSONObject createdAtField = new JSONObject();
        createdAtField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FIELD_NAME,
                APIConstants.AI.VECTOR_DB_PROVIDER_CREATED_AT);
        createdAtField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA_TYPE,
                APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_INT64);
        fields.put(createdAtField);

        // api_id field
        JSONObject apiIdField = new JSONObject();
        apiIdField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FIELD_NAME,
                APIConstants.AI.VECTOR_DB_PROVIDER_API_ID);
        apiIdField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA_TYPE,
                APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_VARCHAR);
        JSONObject apiIdParams = new JSONObject();
        apiIdParams.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_MAX_LENGTH, 36);
        apiIdField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_ELEMENT_TYPE_PARAMS, apiIdParams);
        fields.put(apiIdField);

        // embedding field
        JSONObject embeddingField = new JSONObject();
        embeddingField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FIELD_NAME,
                APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING);
        embeddingField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA_TYPE,
                APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FLOAT_VECTOR);
        JSONObject embeddingParams = new JSONObject();
        embeddingParams.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DIMENSION, dimension);
        embeddingField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_ELEMENT_TYPE_PARAMS, embeddingParams);
        fields.put(embeddingField);

        // response field
        JSONObject responseField = new JSONObject();
        responseField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FIELD_NAME,
                APIConstants.AI.VECTOR_DB_PROVIDER_RESPONSE);
        responseField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA_TYPE,
                APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_VARCHAR);
        JSONObject responseParams = new JSONObject();
        responseParams.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_MAX_LENGTH, 65535);
        responseField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_ELEMENT_TYPE_PARAMS, responseParams);
        responseField.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_IS_NULLABLE, false);
        fields.put(responseField);

        schema.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FIELDS, fields);
        return schema;
    }

    @Override
    public void store(double[] embeddings, CachableResponse response, Map<String, String> filter) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Storing embeddings in Zilliz for API ID: " + filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID));
        }
        if (embeddings == null || embeddings.length != dimension) {
            throw new APIManagementException("Invalid embedding dimension. Expected: " + dimension +
                    ", Received: " + (embeddings != null ? embeddings.length : "null"));
        }
        if (filter == null || !filter.containsKey(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID)) {
            throw new APIManagementException("Missing required filter: 'api_id'");
        }

        // Check if collection exists
        String insertUrl = uri + APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_INSERT_ENDPOINT;

        JSONObject entity = new JSONObject();
        entity.put(APIConstants.AI.VECTOR_DB_PROVIDER_ID, UUID.randomUUID().toString());
        entity.put(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID, filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID));
        entity.put(APIConstants.AI.VECTOR_DB_PROVIDER_CREATED_AT, System.currentTimeMillis());
        entity.put(APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING, embeddings);
        entity.put(APIConstants.AI.VECTOR_DB_PROVIDER_RESPONSE, gson.toJson(response));

        JSONArray dataArr = new JSONArray();
        dataArr.put(entity);

        JSONObject insertPayload = new JSONObject();
        insertPayload.put(APIConstants.AI.VECTOR_DB_PROVIDER_COLLECTION_NAME, collectionName);
        insertPayload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA, dataArr);

        try (CloseableHttpResponse insertResponse = APIUtil.invokeZillizAPI(insertUrl, token, insertPayload.toString())) {
            int insertStatusCode = insertResponse.getStatusLine().getStatusCode();
            if (insertStatusCode != HttpStatus.SC_OK) {
                String errorMsg = "Failed to insert entity for API ID " + filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID)
                        + ": " + EntityUtils.toString(insertResponse.getEntity());
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            } else {
                log.info("Successfully stored response in Zilliz for API ID: " +
                        filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID));
            }
        } catch (IOException e) {
            String apiId = filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID);
            String errorMsg = "IO error storing embeddings in Zilliz for API ID " + apiId + ": " + e.getMessage();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Retrieve the most similar response from the vector database.
     */
    @Override
    public CachableResponse retrieve(double[] embeddings, Map<String, String> filter) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving similar response from Zilliz for API ID: " + filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID));
        }
        if (embeddings == null || embeddings.length != dimension) {
            throw new APIManagementException("Invalid embedding dimension. Expected: " + dimension +
                    ", Received: " + (embeddings != null ? embeddings.length : "null"));
        }
        if (filter == null || !filter.containsKey(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID)
                || !filter.containsKey(APIConstants.AI.VECTOR_DB_PROVIDER_THRESHOLD)) {
            throw new APIManagementException("Missing required filter: 'api_id' or 'threshold'");
        }
        try {
            String queryUrl = uri + APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_SEARCH_ENDPOINT;

            JSONObject payload = new JSONObject();
            payload.put(APIConstants.AI.VECTOR_DB_PROVIDER_COLLECTION_NAME, collectionName);

            String filterExpr = APIConstants.AI.VECTOR_DB_PROVIDER_API_ID + " == \"" +
                    filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID) + "\"";
            payload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_FILTER, filterExpr);

            JSONArray dataArr = new JSONArray();
            dataArr.put(embeddings);
            payload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_DATA, dataArr);

            payload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_ANNS_FIELD,
                    APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING);

            JSONArray outputFields = new JSONArray();
            outputFields.put(APIConstants.AI.VECTOR_DB_PROVIDER_RESPONSE);
            payload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_OUTPUT_FIELDS, outputFields);
            payload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_LIMIT, 1);

            // Optionally Set searchParams with metricType and params.radius (threshold)
            JSONObject searchParams = new JSONObject();
            JSONObject params = new JSONObject();
            params.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_METRIC_TYPE,
                    APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_L2);
            params.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_RADIUS,
                    Double.parseDouble(filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_THRESHOLD)));
            searchParams.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_PARAMS, params);
            payload.put(APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_SEARCH_PARAMS, searchParams);

            try (CloseableHttpResponse retrieveResponse = APIUtil.invokeZillizAPI(queryUrl, token, payload.toString())) {

                int responseStatusCode = retrieveResponse.getStatusLine().getStatusCode();
                String responseString = EntityUtils.toString(retrieveResponse.getEntity());

                if (responseStatusCode != HttpStatus.SC_OK) {
                    throw new APIManagementException("Failed to query: " + responseString);
                }

                JSONObject respObj = new JSONObject(responseString);
                JSONArray results = respObj.getJSONArray("data");
                if (results == null || results.length() == 0) {
                    log.debug("No similar responses found in Zilliz");
                    return null;
                }

                JSONObject topResult = (JSONObject) results.get(0);

                String responseJson = (String) topResult.get(APIConstants.AI.VECTOR_DB_PROVIDER_RESPONSE);
                log.debug("Successfully retrieved similar response from Zilliz");

                return gson.fromJson(responseJson, CachableResponse.class);
            }
        } catch (IOException | org.json.JSONException e) {
            log.error("Error retrieving response from Zilliz. Query URL: " + uri
                    + APIConstants.AI.VECTOR_DB_PROVIDER_ZILLIZ_SEARCH_ENDPOINT
                    + ", Collection: " + collectionName + ", Filter: " + filter, e);
            throw new APIManagementException("Error retrieving response from Zilliz (collection: " + collectionName +
                    ", filter: " + filter + "): " + e.getMessage(), e);
        }
    }
}

/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.concurrent.ConcurrentHashMap;

public class MockDatasetPersistMediator extends AbstractMediator {
    private static final ConcurrentHashMap<String, String> mockDatasetMap = new ConcurrentHashMap<>();

    @Override
    public boolean mediate(MessageContext context) {
        try {
            String openAPIString = (String) context.getProperty(APIMgtGatewayConstants.OPEN_API_STRING);
            JsonObject openAPI = JsonParser.parseString(openAPIString).getAsJsonObject();
            String dbKey = Integer.toString(
                    (context.getProperty(APIMgtGatewayConstants.API_UUID_PROPERTY) + (openAPI.get(
                            APIConstants.X_WSO2_MOCK_DATASET) != null ?
                            openAPI.get(APIConstants.X_WSO2_MOCK_DATASET).getAsString() :
                            "")).hashCode());

            if (context.getProperty(APIConstants.MOCK_DATASET) == null) { // Only for request
                if (!mockDatasetMap.containsKey(dbKey)) {
                    setmockDataset(openAPI, dbKey);
                    if (log.isDebugEnabled()) {
                        log.debug("mock dataset Initialized: " + mockDatasetMap.get(dbKey));
                    }
                }
                // Set the mockDataset to the context
                context.setProperty(APIConstants.MOCK_DATASET, mockDatasetMap.get(dbKey));
                if (log.isDebugEnabled()) {
                    log.debug("mock dataset set to Context: " + mockDatasetMap.get(dbKey));
                }
            } else { // Only for response
                // Update the mockDataset for the specific API
                updatemockDataset(dbKey, (String) context.getProperty(APIConstants.MOCK_DATASET));
                if (log.isDebugEnabled()) {
                    log.debug("mock dataset Updated: " + mockDatasetMap.get(dbKey));
                }
            }
            return true; // Successful mediation
        } catch (Exception e) {
            log.error("Error in mockDatasetPersistMediator: " + e.getMessage());
            return false;
        }
    }

    private void setmockDataset(JsonObject openAPI, String apiKey) {
        if (!openAPI.has(APIConstants.X_WSO2_MOCK_DATASET)) {
            log.error("x-wso2-mock-dataset field not found in open API definition.");
        } else {
            updatemockDataset(apiKey, openAPI.get(APIConstants.X_WSO2_MOCK_DATASET).getAsString());
        }
    }

    private void updatemockDataset(String apiKey, String mockDataset) {
        mockDatasetMap.put(apiKey, mockDataset);
    }
}

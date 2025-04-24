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
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.concurrent.ConcurrentHashMap;

public class MockDBPersistMediator extends AbstractMediator {
    private static final ConcurrentHashMap<String, String> mockDBMap = new ConcurrentHashMap<>();
    final String API_UUID = "API_UUID";
    final String MOCK_DB = "mockDB";
    final String OPEN_API_STRING = "OPEN_API_STRING";

    @Override
    public boolean mediate(MessageContext context) {
        try {
            String openAPIString = (String) context.getProperty(OPEN_API_STRING);
            JsonObject openAPI = JsonParser.parseString(openAPIString).getAsJsonObject();
            String apiKey = Integer.toString(
                    (context.getProperty(API_UUID) +
                            (openAPI.get(APIConstants.X_WSO2_MOCKDB) != null ?
                                    openAPI.get(APIConstants.X_WSO2_MOCKDB).getAsString() : "")).hashCode());

            if (context.getProperty(MOCK_DB) == null) { // Only for request
                if (!mockDBMap.containsKey(apiKey)) {
                    setMockDB(openAPI, apiKey);
                    System.out.println("MockDB Initialized: " + mockDBMap.get(apiKey));
                }
                // Set the mockDB to the context
                context.setProperty(MOCK_DB, mockDBMap.get(apiKey));
                System.out.println("Set mockDB to context: " + mockDBMap.get(apiKey));
            } else { // Only for response
                // Update the mockDB for the specific API
                updateMockDB(apiKey, (String) context.getProperty(MOCK_DB));
                System.out.println("Updated mockDB: " + mockDBMap.get(apiKey));
            }
            return true; // Successful mediation
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setMockDB(JsonObject openAPI, String apiKey) {
        if (!openAPI.has(APIConstants.X_WSO2_MOCKDB)) {
            System.err.println("Error: x-wso2-mockdb field not found in OpenAPI");
        } else {
            updateMockDB(apiKey, openAPI.get(APIConstants.X_WSO2_MOCKDB).getAsString());
        }
    }

    private void updateMockDB(String apiKey, String mockDB) {
        mockDBMap.put(apiKey, mockDB);
    }
}

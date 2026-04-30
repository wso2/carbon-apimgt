/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.UUID;

public class APIKeyAssociationEvent extends Event {
    private String apiKeyHash;
    private String applicationUUId;
    private String apiUUId;
    private int apiId;
    private int applicationId;

    public APIKeyAssociationEvent() {
    }

    public APIKeyAssociationEvent(String type, String apiKeyHash, String applicationUUId, String apiUUId, int apiId,
                                  int applicationId, int tenantId, String tenantDomain) {
        super(UUID.randomUUID().toString(), System.currentTimeMillis(), type, tenantId, tenantDomain);
        this.apiKeyHash = apiKeyHash;
        this.applicationUUId = applicationUUId;
        this.apiUUId = apiUUId;
        this.apiId = apiId;
        this.applicationId = applicationId;
    }

    public APIKeyAssociationEvent(String type,String apiKeyHash, String applicationUUId,
                                  int applicationId,int tenantId,String tenantDomain) {
        super(UUID.randomUUID().toString(), System.currentTimeMillis(), type, tenantId, tenantDomain);
        this.apiKeyHash = apiKeyHash;
        this.applicationUUId = applicationUUId;
        this.applicationId = applicationId;
    }
    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public String getApplicationUUId() {
        return applicationUUId;
    }

    public void setApplicationUUId(String applicationUUId) {
        this.applicationUUId = applicationUUId;
    }

    public String getApiUUId() {
        return apiUUId;
    }

    public void setApiUUId(String apiUUId) {
        this.apiUUId = apiUUId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }
}

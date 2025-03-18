/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package org.wso2.carbon.apimgt.solace.api.v2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The model class for Solace APIs, adhering to the Integrated API response format.
 */
public class IntegratedSolaceApisResponse {

    @SerializedName("data")
    private List<EventApi> integratedSolaceEventApis;

    public List<EventApi> getIntegratedSolaceEventApis() {
        return integratedSolaceEventApis;
    }

    public void setIntegratedSolaceEventApis(List<EventApi> integratedSolaceEventApis) {
        this.integratedSolaceEventApis = integratedSolaceEventApis;
    }

    public static class EventApi {
        @SerializedName("apiId")
        private String apiId;

        @SerializedName("apiName")
        private String apiName;

        @SerializedName("plans")
        private List<String> plans;

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }

        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public List<String> getPlans() {
            return plans;
        }

        public void setPlans(List<String> plans) {
            this.plans = plans;
        }
    }
}

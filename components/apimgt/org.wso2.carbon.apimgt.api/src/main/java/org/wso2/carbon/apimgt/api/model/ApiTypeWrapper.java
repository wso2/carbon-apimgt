/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

public class ApiTypeWrapper {
    private API api;
    private APIProduct apiProduct;
    private boolean isAPIProduct;

    public ApiTypeWrapper(API api) {
        this.api = api;
        isAPIProduct = false;
    }

    public ApiTypeWrapper(APIProduct apiProduct) {
        this.apiProduct = apiProduct;
        isAPIProduct = true;
    }

    public boolean isAPIProduct() {
        return isAPIProduct;
    }

    public API getApi() {
        return api;
    }

    public APIProduct getApiProduct() {
        return apiProduct;
    }

    public String getName() {
        if (isAPIProduct) {
            return apiProduct.getId().getName();
        } else {
            return api.getId().getName();
        }
    }

    public String getStatus() {
        if (isAPIProduct) {
            return apiProduct.getState();
        } else {
            return api.getStatus();
        }
    }

    public void setTier(String tier) {
        if (isAPIProduct) {
            apiProduct.getId().setTier(tier);
        } else {
            api.getId().setTier(tier);
        }
    }
}

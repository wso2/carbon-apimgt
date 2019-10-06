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

import java.io.Serializable;

/**
 * API Product identifier. 
 *
 */
public class APIProductIdentifier implements Serializable, Identifier {

    private static final long serialVersionUID = 1L;
    
    private final String providerName;
    private final String apiProductName;
    //In this initial api product implementation versioning is not supported, we are setting a default version to all api products
    //however we will create these models in such a way so that versioning can be easily introduced later.
    private final String version;
    private String tier;
    private String applicationId;
    private String uuid;
    private int productId;
    
    public APIProductIdentifier(String providerName, String apiProductName, String version){
        this.apiProductName = apiProductName;
        this.providerName = providerName;
        this.version = "1.0.0";
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APIProductIdentifier that = (APIProductIdentifier) o;

        return apiProductName.equals(that.apiProductName) && providerName.equals(that.providerName);
    }

    @Override
    public int hashCode() {
        int result = providerName.hashCode();
        result = 31 * result + apiProductName.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return this.getProviderName() + '-' + this.getName();
    }

    @Override
    public String getName() {
        // API name of the product is the product name
        return apiProductName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }
}

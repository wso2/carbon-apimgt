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
        return isAPIProduct ? apiProduct.getId().getName() : api.getId().getName();
    }

    public String getStatus() {
        return isAPIProduct ? apiProduct.getState() : api.getStatus();
    }

    public void setTier(String tier) {
        if (isAPIProduct) {
            apiProduct.getId().setTier(tier);
        } else {
            api.getId().setTier(tier);
        }
    }

    public String getTier() {
        if (isAPIProduct) {
            return apiProduct.getId().getTier();
        } else {
            return api.getId().getTier();
        }
    }

    public void setContext(String context) {
        if (isAPIProduct) {
            apiProduct.setContext(context);
        } else {
            api.setContext(context);
        }
    }

    public void setAsDefaultVersion(boolean value) {
        if (isAPIProduct) {
            apiProduct.setDefaultVersion(value);
        } else {
            api.setDefaultVersion(value);
        }
    }

    public void setAsPublishedDefaultVersion(boolean value) {
        if (isAPIProduct) {
            apiProduct.setAsPublishedDefaultVersion(value);
        } else {
            api.setAsPublishedDefaultVersion(value);
        }
    }

    public String getContext() {
        return isAPIProduct ? apiProduct.getContext() : api.getContext();
    }

    public void setContextTemplate(String contextTemplate) {
        if (isAPIProduct) {
            apiProduct.setContextTemplate(contextTemplate);
        } else {
            api.setContextTemplate(contextTemplate);
        }
    }

    public String getContextTemplate() {
        return isAPIProduct ? apiProduct.getContextTemplate() : api.getContextTemplate();
    }

    public Identifier getId() {
        return isAPIProduct ? apiProduct.getId() : api.getId();
    }

    public String getUuid() {
        if (isAPIProduct) {
            return apiProduct.getUuid();
        } else {
            return api.getUuid();
        }
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        if (isAPIProduct) {
            apiProduct.setThumbnailUrl(thumbnailUrl);
        } else {
            api.setThumbnailUrl(thumbnailUrl);
        }
    }

    public String getVisibleRoles() {
        return isAPIProduct ? apiProduct.getVisibleRoles() : api.getVisibleRoles();
    }

    public String getVisibility() {
        return isAPIProduct ? apiProduct.getVisibility() : api.getVisibility();
    }

    public String getOrganization() {

        if (isAPIProduct) {
            return apiProduct.getOrganization();
        } else {
            return api.getOrganization();
        }
    }

    public void setOrganization(String organization) {
        if (isAPIProduct) {
            apiProduct.setOrganization(organization);
        } else {
            api.setOrganization(organization);
        }
    }

    public String getLifecycleState() {
        if (isAPIProduct) {
            return apiProduct.getState();
        } else {
            return api.getStatus();
        }
    }

    public String getAccessControl() {
        if (isAPIProduct) {
            return apiProduct.getAccessControl();
        }
        return api.getAccessControl();
    }

    public String getAccessControlRoles() {
        if (isAPIProduct) {
            return apiProduct.getAccessControlRoles();
        }
        return api.getAccessControlRoles();
    }

    public String getType() {
        if (isAPIProduct){
            return apiProduct.getType();
        }
        return api.getType();
    }

    public boolean getPublishedDefaultVersion() {
        if (isAPIProduct){
            return apiProduct.isPublishedDefaultVersion();
        }
        return api.isPublishedDefaultVersion();
    }

    public boolean getDefaultVersion() {
        if (isAPIProduct){
            return apiProduct.isDefaultVersion();
        }
        return api.isDefaultVersion();
    }
}

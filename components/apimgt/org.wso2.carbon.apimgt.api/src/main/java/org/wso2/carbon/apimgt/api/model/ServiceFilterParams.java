/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.api.model;

/**
 * Service Filter Parameters Object.
 */
public class ServiceFilterParams {

    String name;

    String version;

    String definitionType;

    String displayName;

    String key;

    String sortBy;

    String sortOrder;

    int limit;

    int offset;

    public String getName() {

        return name;
    }

    public String getVersion() {

        return version;
    }

    public String getDefinitionType() {

        return definitionType;
    }

    public String getDisplayName() {

        return displayName;
    }

    public String getKey() {

        return key;
    }

    public String getSortBy() {

        return sortBy;
    }

    public String getSortOrder() {

        return sortOrder;
    }

    public int getLimit() {

        return limit;
    }

    public int getOffset() {

        return offset;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public void setDefinitionType(String definitionType) {

        this.definitionType = definitionType;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public void setSortBy(String sortBy) {

        this.sortBy = sortBy;
    }

    public void setSortOrder(String sortOrder) {

        this.sortOrder = sortOrder;
    }

    public void setLimit(int limit) {

        this.limit = limit;
    }

    public void setOffset(int offset) {

        this.offset = offset;
    }
}

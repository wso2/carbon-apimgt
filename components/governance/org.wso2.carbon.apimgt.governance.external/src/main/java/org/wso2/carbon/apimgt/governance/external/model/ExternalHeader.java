/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.external.model;

/**
 * Model for an external governance header with key, value, and category.
 */
public class ExternalHeader {

    private String key;
    private Object value;
    private String category;

    public ExternalHeader() {
    }

    public ExternalHeader(String key, Object value, String category) {

        this.key = key;
        this.value = value;
        this.category = category;
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public Object getValue() {

        return value;
    }

    public void setValue(Object value) {

        this.value = value;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }

    @Override
    public String toString() {

        return "ExternalHeader{" +
                "key='" + key + '\'' +
                ", value=" + value +
                ", category='" + category + '\'' +
                '}';
    }
}

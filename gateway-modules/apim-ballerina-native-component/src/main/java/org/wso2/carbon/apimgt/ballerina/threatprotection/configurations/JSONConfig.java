/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.ballerina.threatprotection.configurations;

/**
 * Configuration holding class for {@link org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.JSONAnalyzer}
 */
public class JSONConfig {
    private String name = "";
    private int maxPropertyCount = 0;
    private int maxStringLength = 0;
    private int maxArrayElementCount = 0;
    private int maxKeyLength = 0;
    private int maxJsonDepth = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxPropertyCount() {
        return maxPropertyCount;
    }

    public int getMaxStringLength() {
        return maxStringLength;
    }

    public int getMaxArrayElementCount() {
        return maxArrayElementCount;
    }

    public int getMaxKeyLength() {
        return maxKeyLength;
    }

    public int getMaxJsonDepth() {
        return maxJsonDepth;
    }

    public void setMaxPropertyCount(int maxPropertyCount) {
        this.maxPropertyCount = maxPropertyCount;
    }

    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    public void setMaxArrayElementCount(int maxArrayElementCount) {
        this.maxArrayElementCount = maxArrayElementCount;
    }

    public void setMaxKeyLength(int maxKeyLength) {
        this.maxKeyLength = maxKeyLength;
    }

    public void setMaxJsonDepth(int maxJsonDepth) {
        this.maxJsonDepth = maxJsonDepth;
    }
}

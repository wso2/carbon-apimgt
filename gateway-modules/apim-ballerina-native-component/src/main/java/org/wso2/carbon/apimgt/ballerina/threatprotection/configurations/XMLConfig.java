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
 * Configuration holding class for {@link org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.XMLAnalyzer}
 */
public class XMLConfig {
    private String name;
    private boolean dtdEnabled;
    private boolean externalEntitiesEnabled;
    private int maxDepth;
    private int maxElementCount;
    private int maxAttributeCount;
    private int maxAttributeLength;
    private int entityExpansionLimit;
    private int maxChildrenPerElement;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDtdEnabled() {
        return dtdEnabled;
    }

    public boolean isExternalEntitiesEnabled() {
        return externalEntitiesEnabled;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getMaxElementCount() {
        return maxElementCount;
    }

    public int getMaxAttributeCount() {
        return maxAttributeCount;
    }

    public int getMaxAttributeLength() {
        return maxAttributeLength;
    }

    public int getEntityExpansionLimit() {
        return entityExpansionLimit;
    }

    public int getMaxChildrenPerElement() {
        return maxChildrenPerElement;
    }

    public void setDtdEnabled(boolean dtdEnabled) {
        this.dtdEnabled = dtdEnabled;
    }

    public void setExternalEntitiesEnabled(boolean externalEntitiesEnabled) {
        this.externalEntitiesEnabled = externalEntitiesEnabled;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setMaxElementCount(int maxElementCount) {
        this.maxElementCount = maxElementCount;
    }

    public void setMaxAttributeCount(int maxAttributeCount) {
        this.maxAttributeCount = maxAttributeCount;
    }

    public void setMaxAttributeLength(int maxAttributeLength) {
        this.maxAttributeLength = maxAttributeLength;
    }

    public void setEntityExpansionLimit(int entityExpansionLimit) {
        this.entityExpansionLimit = entityExpansionLimit;
    }

    public void setMaxChildrenPerElement(int maxChildrenPerElement) {
        this.maxChildrenPerElement = maxChildrenPerElement;
    }
}

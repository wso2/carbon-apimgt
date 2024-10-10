/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api;

public class TokenBasedThrottlingCountHolder {

    private String productionMaxPromptTokenCount;
    private String productionMaxCompletionTokenCount;
    private String productionMaxTotalTokenCount;
    private String sandboxMaxPromptTokenCount;
    private String sandboxMaxCompletionTokenCount;
    private String sandboxMaxTotalTokenCount;
    private Boolean isTokenBasedThrottlingEnabled = false;

    public TokenBasedThrottlingCountHolder() {

    }

    public TokenBasedThrottlingCountHolder(String productionMaxPromptTokenCount, String productionMaxCompletionTokenCount,
            String productionMaxTotalTokenCount, String sandboxMaxPromptTokenCount,
            String sandboxMaxCompletionTokenCount, String sandboxMaxTotalTokenCount,
                                          boolean isTokenBasedThrottlingEnabled) {
        this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
        this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
        this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
        this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
        this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
        this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
        this.isTokenBasedThrottlingEnabled = isTokenBasedThrottlingEnabled;
    }

    public String getProductionMaxPromptTokenCount() {
        return productionMaxPromptTokenCount;
    }

    public void setProductionMaxPromptTokenCount(String productionMaxPromptTokenCount) {
        this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
    }

    public String getProductionMaxCompletionTokenCount() {
        return productionMaxCompletionTokenCount;
    }

    public void setProductionMaxCompletionTokenCount(String productionMaxCompletionTokenCount) {
        this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
    }

    public String getProductionMaxTotalTokenCount() {
        return productionMaxTotalTokenCount;
    }

    public void setProductionMaxTotalTokenCount(String productionMaxTotalTokenCount) {
        this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
    }

    public String getSandboxMaxPromptTokenCount() {
        return sandboxMaxPromptTokenCount;
    }

    public void setSandboxMaxPromptTokenCount(String sandboxMaxPromptTokenCount) {
        this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
    }

    public String getSandboxMaxCompletionTokenCount() {
        return sandboxMaxCompletionTokenCount;
    }

    public void setSandboxMaxCompletionTokenCount(String sandboxMaxCompletionTokenCount) {
        this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
    }

    public String getSandboxMaxTotalTokenCount() {
        return sandboxMaxTotalTokenCount;
    }

    public void setSandboxMaxTotalTokenCount(String sandboxMaxTotalTokenCount) {
        this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
    }

    public Boolean isTokenBasedThrottlingEnabled() {
        return isTokenBasedThrottlingEnabled;
    }

    public void setTokenBasedThrottlingEnabled(Boolean tokenBasedThrottlingEnabled) {
        isTokenBasedThrottlingEnabled = tokenBasedThrottlingEnabled;
    }
}
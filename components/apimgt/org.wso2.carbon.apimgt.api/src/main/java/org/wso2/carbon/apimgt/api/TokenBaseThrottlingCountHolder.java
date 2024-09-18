/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

public class TokenBaseThrottlingCountHolder {

    private Long productionMaxPromptTokenCount = -1L;
    private Long productionMaxCompletionTokenCount = -1L;
    private Long productionMaxTotalTokenCount = -1L;
    private Long sandboxMaxPromptTokenCount = -1L;
    private Long sandboxMaxCompletionTokenCount = -1L;
    private Long sandboxMaxTotalTokenCount = -1L;
    private boolean isTokenBasedThrottlingEnabled = false;

    public TokenBaseThrottlingCountHolder() {

    }

    public TokenBaseThrottlingCountHolder(Long productionMaxPromptTokenCount, Long productionMaxCompletionTokenCount,
                                          Long productionMaxTotalTokenCount, Long sandboxMaxPromptTokenCount,
                                          Long sandboxMaxCompletionTokenCount, Long sandboxMaxTotalTokenCount,
                                          boolean isTokenBasedThrottlingEnabled) {
        this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
        this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
        this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
        this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
        this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
        this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
        this.isTokenBasedThrottlingEnabled = isTokenBasedThrottlingEnabled;
    }

    public Long getProductionMaxPromptTokenCount() {
        return productionMaxPromptTokenCount;
    }

    public void setProductionMaxPromptTokenCount(Long productionMaxPromptTokenCount) {
        this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
    }

    public Long getProductionMaxCompletionTokenCount() {
        return productionMaxCompletionTokenCount;
    }

    public void setProductionMaxCompletionTokenCount(Long productionMaxCompletionTokenCount) {
        this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
    }

    public Long getProductionMaxTotalTokenCount() {
        return productionMaxTotalTokenCount;
    }

    public void setProductionMaxTotalTokenCount(Long productionMaxTotalTokenCount) {
        this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
    }

    public Long getSandboxMaxPromptTokenCount() {
        return sandboxMaxPromptTokenCount;
    }

    public void setSandboxMaxPromptTokenCount(Long sandboxMaxPromptTokenCount) {
        this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
    }

    public Long getSandboxMaxCompletionTokenCount() {
        return sandboxMaxCompletionTokenCount;
    }

    public void setSandboxMaxCompletionTokenCount(Long sandboxMaxCompletionTokenCount) {
        this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
    }

    public Long getSandboxMaxTotalTokenCount() {
        return sandboxMaxTotalTokenCount;
    }

    public void setSandboxMaxTotalTokenCount(Long sandboxMaxTotalTokenCount) {
        this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
    }

    public boolean isTokenBasedThrottlingEnabled() {
        return isTokenBasedThrottlingEnabled;
    }

    public void setTokenBasedThrottlingEnabled(boolean tokenBasedThrottlingEnabled) {
        isTokenBasedThrottlingEnabled = tokenBasedThrottlingEnabled;
    }
}
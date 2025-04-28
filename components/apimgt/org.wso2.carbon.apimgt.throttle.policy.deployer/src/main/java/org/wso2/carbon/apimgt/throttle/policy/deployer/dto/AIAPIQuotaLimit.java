/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.throttle.policy.deployer.dto;

/**
 * Entity for keeping details of a AI Quota Limit
 */
public class AIAPIQuotaLimit extends Limit {

    private long requestCount;
    private long totalTokenCount;
    private long promptTokenCount;
    private long completionTokenCount;

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public long getTotalTokenCount() {
        return totalTokenCount;
    }

    public void setTotalTokenCount(long totalTokenCount) {
        this.totalTokenCount = totalTokenCount;
    }

    public long getPromptTokenCount() {
        return promptTokenCount;
    }

    public void setPromptTokenCount(long promptTokenCount) {
        this.promptTokenCount = promptTokenCount;
    }

    public long getCompletionTokenCount() {
        return completionTokenCount;
    }

    public void setCompletionTokenCount(long completionTokenCount) {
        this.completionTokenCount = completionTokenCount;
    }
}

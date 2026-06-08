/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.List;
import java.util.Map;

/**
 * Event that carries the state of an on-demand federated API discovery task so that
 * every CP node in a cluster can keep its local task store in sync via the JMS
 * notification topic.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code taskId}      – UUID assigned to the discovery job on the originating node</li>
 *   <li>{@code envKey}      – composite key "{organization}|{environmentName}" used for de-dup</li>
 *   <li>{@code status}      – PENDING | COMPLETED | FAILED</li>
 *   <li>{@code organization}– tenant organization</li>
 *   <li>{@code originNodeId}– node ID that generated this event (used to skip self-processing)</li>
 *   <li>{@code result}      – discovered API list (populated only for COMPLETED events)</li>
 *   <li>{@code errorMessage}– failure reason (populated only for FAILED events)</li>
 * </ul>
 */
public class FederatedDiscoverySyncEvent extends Event {

    private String taskId;
    private String envKey;
    private String status;
    private String organization;
    private String originNodeId;
    private List<Map<String, Object>> result;
    private String errorMessage;

    public FederatedDiscoverySyncEvent(String taskId, String envKey, String status,
            String organization, String originNodeId) {

        this.taskId = taskId;
        this.envKey = envKey;
        this.status = status;
        this.organization = organization;
        this.originNodeId = originNodeId;
        this.type = "FEDERATED_DISCOVERY_SYNC";
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getEnvKey() {
        return envKey;
    }

    public void setEnvKey(String envKey) {
        this.envKey = envKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOriginNodeId() {
        return originNodeId;
    }

    public void setOriginNodeId(String originNodeId) {
        this.originNodeId = originNodeId;
    }

    public List<Map<String, Object>> getResult() {
        return result;
    }

    public void setResult(List<Map<String, Object>> result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

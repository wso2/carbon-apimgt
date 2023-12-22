/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.Set;

/**
 * This class represents the Gateway Policy Event which is fired when a gateway policy mapping is added or deleted.
 */
public class GatewayPolicyEvent extends Event{

    private String gatewayPolicyMappingUuid;
    private Set<String> gatewayLabels;

    public GatewayPolicyEvent(String eventId, long timestamp, String type, String tenantDomain, String gatewayPolicyMappingUuid,
                              Set<String> gatewayLabels) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantDomain = tenantDomain;
        this.gatewayPolicyMappingUuid = gatewayPolicyMappingUuid;
        this.gatewayLabels = gatewayLabels;
    }

    public String getGatewayPolicyMappingUuid() {
        return gatewayPolicyMappingUuid;
    }

    public void setGatewayPolicyMappingUuid(String gatewayPolicyMappingUuid) {
        this.gatewayPolicyMappingUuid = gatewayPolicyMappingUuid;
    }

    public Set<String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }
}

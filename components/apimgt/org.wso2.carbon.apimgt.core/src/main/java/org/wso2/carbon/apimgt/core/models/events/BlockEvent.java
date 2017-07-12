/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.models.events;

/**
 * Holds the Block condition related details transferred to Gateway
 */
public class BlockEvent extends GatewayEvent {


    private boolean enabled;
    private String conditionType, conditionValue;
    private int conditionId;
    private String uuid;
    private long fixedIp;
    private long startingIP;
    private long endingIP;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public int getConditionId() {
        return conditionId;
    }

    public void setConditionId(int conditionId) {
        this.conditionId = conditionId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getStartingIP() {
        return startingIP;
    }

    public void setStartingIP(long startingIP) {
        this.startingIP = startingIP;
    }

    public long getEndingIP() {
        return endingIP;
    }

    public void setEndingIP(long endingIP) {
        this.endingIP = endingIP;
    }

    public BlockEvent(String eventType) {
        super(eventType);
    }

    public long getFixedIp() {
        return fixedIp;
    }

    public void setFixedIp(long fixedIp) {
        this.fixedIp = fixedIp;
    }
}

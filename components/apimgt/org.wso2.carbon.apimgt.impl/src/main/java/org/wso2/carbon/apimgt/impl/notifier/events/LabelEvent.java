/*
 *
 * Copyright (c), 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

/**
 * This class represents the event for label related operations
 */
public class LabelEvent extends Event {

    private String labelId;
    private String name;
    private String description;

    public LabelEvent(String eventId, long timestamp, String type, String tenantDomain, String labelId,
                      String name) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantDomain = tenantDomain;
        this.labelId = labelId;
        this.name = name;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.notifier.events;

public class KeyTemplateEvent extends Event {
    private String keyTemplateState;
    private String keyTemplate;
    private String oldKeyTemplate;
    private String newKeyTemplate;

    public KeyTemplateEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
                            String keyTemplateState, String keyTemplate) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.keyTemplateState = keyTemplateState;
        this.keyTemplate = keyTemplate;
    }

    public KeyTemplateEvent(String eventId, long timestamp, int tenantId, String tenantDomain, String type,
                            String keyTemplateState, String oldKeyTemplate, String newKeyTemplate) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.keyTemplateState = keyTemplateState;
        this.oldKeyTemplate = oldKeyTemplate;
        this.newKeyTemplate = newKeyTemplate;
    }

    public String getKeyTemplateState() {
        return keyTemplateState;
    }

    public void setKeyTemplateState(String keyTemplateState) {
        this.keyTemplateState = keyTemplateState;
    }

    public String getKeyTemplate() {
        return keyTemplate;
    }

    public void setKeyTemplate(String keyTemplate) {
        this.keyTemplate = keyTemplate;
    }

    public String getOldKeyTemplate() {
        return oldKeyTemplate;
    }

    public void setOldKeyTemplate(String oldKeyTemplate) {
        this.oldKeyTemplate = oldKeyTemplate;
    }

    public String getNewKeyTemplate() {
        return newKeyTemplate;
    }

    public void setNewKeyTemplate(String newKeyTemplate) {
        this.newKeyTemplate = newKeyTemplate;
    }

}

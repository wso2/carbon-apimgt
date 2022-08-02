/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

/**
 * An Event Object which can holds the data related to Application Certificate which are required
 * for the validation purpose in a gateway.
 */

public class ApplicationCertificateEvent extends Event {
    private int applicationId;
    private String UUID;
    private String name;




    public ApplicationCertificateEvent(String eventId, long timeStamp, String type, String tenantDomain, int applicationId, String uuid) {

        this.applicationId = applicationId;
        this.UUID = uuid;
       // this.name = name;
        this.eventId = eventId;
        this.timeStamp = timeStamp;
        this.type = type;
        this.tenantDomain = tenantDomain;
    }

    public int getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {

        this.applicationId = applicationId;
    }

    public String getUUID() {

        return UUID;
    }

    public void setUUID(String UUID) {

        this.UUID = UUID;
    }
//    public String getName() {
//
//        return name;
//    }
//
//    public void setName(String name) {
//
//        this.name = name;
//    }


}



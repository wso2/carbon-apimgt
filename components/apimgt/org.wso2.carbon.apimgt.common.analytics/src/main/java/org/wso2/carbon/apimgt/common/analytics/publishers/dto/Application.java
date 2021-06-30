/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.common.analytics.publishers.dto;

/**
 * Application attribute in analytics event.
 */
public class Application {

    private String keyType;
    private String applicationId;
    private String applicationName;
    private String applicationOwner;

    public String getKeyType() {

        return keyType;
    }

    public void setKeyType(String keyType) {

        this.keyType = keyType;
    }

    public String getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(String applicationId) {

        this.applicationId = applicationId;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    public String getApplicationOwner() {

        return applicationOwner;
    }

    public void setApplicationOwner(String applicationOwner) {

        this.applicationOwner = applicationOwner;
    }
}

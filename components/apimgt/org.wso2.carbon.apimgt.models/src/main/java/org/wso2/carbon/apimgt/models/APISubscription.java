/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Subscriber's view of the API
 */

public class APISubscription {

    private APIIdentifier apiId;
    private Application application;
    private String uuid;

    public APISubscription(Application application, APIIdentifier apiId) {
        this.application = application;
        this.apiId = apiId;
    }

    public Application getApplication() {
        return application;
    }

    public APIIdentifier getApiId() {
        return apiId;
    }


    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APISubscription that = (APISubscription) o;
        return apiId.equals(that.apiId) && application.equals(that.application);
    }

    @Override
    public int hashCode() {
        int result = apiId.hashCode();
        result = 31 * result + application.hashCode();
        return result;
    }
}

/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.apk.apimgt.rest.api.store.v1.common.models;

import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.Subscriber;

/**
 * Model class for ExportedSubscribedAPI
 */
public class ExportedSubscribedAPI {
        private APIIdentifier apiId;
        private Subscriber subscriber;
        private String throttlingPolicy;

    public ExportedSubscribedAPI(APIIdentifier apiId, Subscriber subscriber, String throttlingPolicy) {
        this.apiId = apiId;
        this.subscriber = subscriber;
        this.throttlingPolicy = throttlingPolicy;
    }

    public APIIdentifier getApiId() {
        return apiId;
    }

    public void setApiId(APIIdentifier apiId) {
        this.apiId = apiId;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public String getThrottlingPolicy() {
        return throttlingPolicy;
    }

    public void setThrottlingPolicy(String throttlingPolicy) {
        this.throttlingPolicy = throttlingPolicy;
    }
}

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used as a pojo class to represent API count by subscribers
 */
public class SubscriberCountByAPIs {
    private List<String> apiName = new ArrayList<String>();
    private long count;

    public List<String> getApiName() {
        return apiName;
    }

    public void setApiName(List<String> apiName) {
        this.apiName = apiName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

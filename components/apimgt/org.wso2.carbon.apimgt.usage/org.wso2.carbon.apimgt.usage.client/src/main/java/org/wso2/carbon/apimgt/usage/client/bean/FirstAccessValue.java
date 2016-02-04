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
package org.wso2.carbon.apimgt.usage.client.bean;

/**
 * This class is used as a bean for represent API First access time result from the DAS REST API
 */
public class FirstAccessValue {
    private long first_access_time;

    public long getFirst_access_time() {
        return first_access_time;
    }

    public void setFirst_access_time(long first_access_time) {
        this.first_access_time = first_access_time;
    }

    public FirstAccessValue(long first_access_time) {
        super();
        this.first_access_time = first_access_time;
    }
}

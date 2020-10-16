/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.throttle.policy.deployer;


public class QuotaPolicy {

    private String quotaType;
    private RequestCountLimit requestCount;
    private BandwidthLimit bandwidth;

    public String getQuotaType() {
        return quotaType;
    }

    public void setQuotaType(String quotaType) {
        this.quotaType = quotaType;
    }

    public RequestCountLimit getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(RequestCountLimit requestCount) {
        this.requestCount = requestCount;
    }

    public BandwidthLimit getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(BandwidthLimit bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Limit getLimit() {
        if (this.requestCount != null) {
            return this.requestCount;
        }
        return this.bandwidth;
    }
}

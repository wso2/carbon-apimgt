/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.throttle.policy.deployer.dto;

import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;

/**
 * Entity for keeping details of a Bandwidth Limit
 */
public class BandwidthLimit extends Limit {
    private long dataAmount;
    private String dataUnit;

    public long getDataAmount() {
        return dataAmount;
    }

    public void setDataAmount(long dataAmount) {
        this.dataAmount = dataAmount;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String dataUnit) {
        this.dataUnit = dataUnit;
    }

    public long getStandardDataAmount() {
        if (PolicyConstants.MB.equalsIgnoreCase(dataUnit)) {
            return dataAmount * 1024 * 1024;
        } else if (PolicyConstants.KB.equalsIgnoreCase(dataUnit)) {
            return dataAmount * 1024;
        }
        return dataAmount;
    }
}
